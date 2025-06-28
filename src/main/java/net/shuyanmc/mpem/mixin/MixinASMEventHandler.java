package net.shuyanmc.mpem.mixin;

import net.neoforged.bus.ConsumerEventHandler;
import org.spongepowered.asm.mixin.Mixin;

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
}