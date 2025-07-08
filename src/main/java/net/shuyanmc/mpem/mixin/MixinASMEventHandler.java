package net.shuyanmc.mpem.mixin;

import net.neoforged.bus.ConsumerEventHandler;
import net.shuyanmc.mpem.MpemMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(value = ConsumerEventHandler.class)
public class MixinASMEventHandler {
    /*
    @Mutable
    @Shadow
    @Final
    private IEventListener handler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initHandler(Object target, Method method, boolean isGeneric, CallbackInfo ci) {
        System.out.println("事件优化正常运行！｜MixinASMEventHandler applied!");
        this.handler = EventListenerFactory.createListener(method, target);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Class;newInstance()Ljava/lang/Object;"))
    private Object bypassASMGeneration(Class<?> clazz) {
        return DummyHandler.INSTANCE;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Constructor;newInstance([Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object bypassConstructorCall(Constructor<?> constructor, Object[] args) {
        return DummyHandler.INSTANCE;
    }*/

    @Redirect(method = "invoke",at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public <T> void onInvoke(Consumer instance, T t){
        try {
            System.out.println(t.getClass().getSimpleName());
            Class.forName(t.getClass().getName());
            instance.accept(t);
        }
        catch (NoClassDefFoundError | Exception e){
            MpemMod.LOGGER.error("Skipped a event.");
        }
    }
}