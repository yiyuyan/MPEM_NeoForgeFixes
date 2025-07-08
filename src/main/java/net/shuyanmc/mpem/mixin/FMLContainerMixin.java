package net.shuyanmc.mpem.mixin;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FMLModContainer.class)
public class FMLContainerMixin {
    @Inject(method = "onEventFailed",at = @At(value = "HEAD"), cancellable = true)
    public void event(IEventBus iEventBus, Event event, EventListener[] iEventListeners, int i, Throwable throwable, CallbackInfo ci){
        if(throwable instanceof ClassNotFoundException || throwable instanceof NoClassDefFoundError){
            ci.cancel();
        }
    }
}
