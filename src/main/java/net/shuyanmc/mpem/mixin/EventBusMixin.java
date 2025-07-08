package net.shuyanmc.mpem.mixin;

import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.shuyanmc.mpem.MpemMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EventBus.class)
public class EventBusMixin {
    @Inject(method = "post*",at = @At("HEAD"), cancellable = true)
    public void post(Object event, CallbackInfo ci){
        try {
            Class.forName(event.getClass().getName());
        }
        catch (NoClassDefFoundError | ClassNotFoundException e){
            MpemMod.LOGGER.error("Skipped a event.");
            System.out.println("[MPEM] SKIPPED A EVENT.");
            ci.cancel();
        }
    }

    @Inject(method = "doPostChecks",at = @At("HEAD"))
    public void check(Event event, CallbackInfo ci){
        try {
            Class.forName(event.getClass().getName());
        }
        catch (NoClassDefFoundError | ClassNotFoundException e){
            System.out.println("[MPEM] SKIPPED A EVENT.");
            try {
                throw new IllegalArgumentException("Cannot post event of type " + event.getClass().getSimpleName() + " to this bus");
            } catch (NoClassDefFoundError | Exception ex) {
                MpemMod.LOGGER.error("error in the event: {}",ex.getMessage());
            }
        }
        MpemMod.LOGGER.error("checked a event.");
    }
}
