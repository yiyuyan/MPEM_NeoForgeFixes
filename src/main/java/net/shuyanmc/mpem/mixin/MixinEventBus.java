package net.shuyanmc.mpem.mixin;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.shuyanmc.mpem.AsyncEventSystem;
import net.shuyanmc.mpem.config.CoolConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Mixin(EventBus.class)
public abstract class MixinEventBus implements IEventBus {

    @Shadow
    protected abstract <T extends Event> Class<T> getEventClass(Consumer<T> consumer);

    @Redirect(
            method = "post(Lnet/neoforged/bus/api/Event;[Lnet/neoforged/bus/api/EventListener;)Lnet/neoforged/bus/api/Event;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/bus/api/EventListener;invoke(Lnet/neoforged/bus/api/Event;)V"
            ),
            remap = false
    )
    private void redirectEventListenerInvoke(EventListener instance, Event event) {
        Class<? extends Event> eventClass = event.getClass();

        // 跳过在服务器上执行的客户端事件
        if (AsyncEventSystem.isClientOnlyEvent(eventClass) && FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            return;
        }

        if (AsyncEventSystem.shouldHandleAsync(eventClass)) {
            CompletableFuture<Void> future = AsyncEventSystem.executeAsync(
                    eventClass,
                    () -> instance.invoke(event)
            );

            if (CoolConfig.WAIT_FOR_ASYNC_EVENTS.get()) {
                try {
                    future.get(CoolConfig.ASYNC_EVENT_TIMEOUT.get(), TimeUnit.SECONDS);
                } catch (Exception e) {
                    AsyncEventSystem.LOGGER.error("Async event timed out", e);
                }
            }
        } else {
            instance.invoke(event);
        }
    }

    @Inject(
            method = "addListener(Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            remap = false
    )
    private <T extends Event> void onAddListener(EventPriority priority, Consumer<T> consumer, CallbackInfo ci) {
        // 跳过在服务器上注册的客户端事件
        Class<T> eventClass = this.getEventClass(consumer);
        if (!AsyncEventSystem.isClientOnlyEvent(eventClass) || FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            AsyncEventSystem.registerAsyncEvent(eventClass);
        }
    }

    @Inject(
            method = "addListener(Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onAddListener(Consumer<Event> consumer, CallbackInfo ci) {
        AsyncEventSystem.tryRegisterAsyncEvent(consumer);
    }
}