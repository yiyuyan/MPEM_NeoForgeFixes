package net.shuyanmc.mpem.mixin;
/*
import net.minecraftforge.eventbus.ASMEventHandler;
import net.minecraftforge.eventbus.api.IEventListener;*/

import net.neoforged.bus.api.EventListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.invoke.MethodHandles;

//@Mixin(value = ASMEventHandler.class, remap = false)
public abstract class EventHandlerMixin {
    @Unique
    private static final MethodHandles.Lookup METHOD_LOOKUP = MethodHandles.lookup();
    @Shadow
    @Final
    @Mutable

    private EventListener handler;
/*
    @Inject(method = "<init>", at = @At("HEAD"))
    private void initHandler(Object target, Method method, boolean isGeneric, CallbackInfo ci) {
        this.handler = FastEventHandler.create(target, method);
        ci.cancel();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", 
              target = "Lnet/minecraftforge/eventbus/ASMEventHandler;createWrapper(Ljava/lang/reflect/Method;)Ljava/lang/Class;"))
    private Class<?> redirectWrapperCreation(Method method) {
        return Void.class;
    }*/
}