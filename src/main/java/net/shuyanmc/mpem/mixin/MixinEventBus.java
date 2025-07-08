package net.shuyanmc.mpem.mixin;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.*;
import net.neoforged.fml.loading.FMLEnvironment;
import net.shuyanmc.mpem.AsyncEventSystem;
import net.shuyanmc.mpem.MpemMod;
import net.shuyanmc.mpem.config.CoolConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        Class<? extends Event> eventClass = null;
        try {
            eventClass = event.getClass();
        } catch (NoClassDefFoundError | Exception e) {
            MpemMod.LOGGER.error("Skipped a class what can't load.");
            return;
        }

        // 跳过在服务器上执行的客户端事件
        if (AsyncEventSystem.isClientOnlyEvent(eventClass) && FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            return;
        }

        if (AsyncEventSystem.shouldHandleAsync(eventClass)) {
            CompletableFuture<Void> future = AsyncEventSystem.executeAsync(
                    eventClass,
                    () -> {
                        try {
                            instance.invoke(event);
                        } catch (NoClassDefFoundError | Exception e) {
                            MpemMod.LOGGER.error("error!");
                        }
                    }
            );

            if (CoolConfig.WAIT_FOR_ASYNC_EVENTS.get()) {
                try {
                    future.get(CoolConfig.ASYNC_EVENT_TIMEOUT.get(), TimeUnit.SECONDS);
                } catch (Exception e) {
                    AsyncEventSystem.LOGGER.error("Async event timed out", e);
                }
            }
        } else {
            try {
                instance.invoke(event);
            } catch (NoClassDefFoundError | Exception e) {
                MpemMod.LOGGER.error("error");
            }

        }
    }

    @Redirect(method = "post(Lnet/neoforged/bus/api/Event;[Lnet/neoforged/bus/api/EventListener;)Lnet/neoforged/bus/api/Event;",at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/IEventExceptionHandler;handleException(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/Event;[Lnet/neoforged/bus/api/EventListener;ILjava/lang/Throwable;)V"))
    private void noPostExp(IEventExceptionHandler instance, IEventBus iEventBus, Event event, EventListener[] eventListeners, int i, Throwable throwable){
        try {
            if(!(throwable instanceof ClassNotFoundException) && !(throwable instanceof NoClassDefFoundError)){
                instance.handleException(iEventBus,event,eventListeners,i,throwable);
            }
        }
        catch (Throwable d){
            MpemMod.LOGGER.error("error in post",d);
        }
    }

    @Inject(
            method = "addListener(Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            remap = false
    )
    private <T extends Event> void onAddListener(EventPriority priority, Consumer<T> consumer, CallbackInfo ci) {
        // 跳过在服务器上注册的客户端事件
        Class<T> eventClass;
        try {
            eventClass = this.getEventClass(consumer);
        } catch (NoClassDefFoundError | Exception e) {
            MpemMod.LOGGER.error("Skipped a class what can't load");
            return;
        }
        if (!AsyncEventSystem.isClientOnlyEvent(eventClass) || FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            AsyncEventSystem.registerAsyncEvent(eventClass);
        }
    }

    @Inject(
            method = "addListener(Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onAddListenerZ(Consumer<Event> consumer, CallbackInfo ci) {
        try {
            Class<?> clazz = getEventClass(consumer);
            System.out.println(clazz.getName());
        } catch (NoClassDefFoundError | Exception e) {
            MpemMod.LOGGER.error("Skipped a class what can't load");
            return;
        }
        AsyncEventSystem.tryRegisterAsyncEvent(consumer);
    }

    @Inject(
            method = "addListener(Ljava/lang/Class;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            remap = false
    )
    private <T extends Event> void onAddListener(Class<T> eventType, Consumer<T> consumer, CallbackInfo ci) {
        try {
            Class.forName(eventType.getName());
        } catch (NoClassDefFoundError | Exception e) {
            MpemMod.LOGGER.error("Skipped a class what can't load");
            return;
        }
        AsyncEventSystem.tryRegisterAsyncEvent(consumer);
    }

    @Inject(
            method = "addListener*",
            at = @At("HEAD"),
            remap = false
    )
    private <T extends Event> void onAddListener(Consumer<T> consumer, CallbackInfo ci) {
        try {
            Class.forName(getEventClass(consumer).getName());
        } catch (NoClassDefFoundError | Exception e) {
            MpemMod.LOGGER.error("Skipped a class what can't load");
            return;
        }
        AsyncEventSystem.tryRegisterAsyncEvent(consumer);
    }
}