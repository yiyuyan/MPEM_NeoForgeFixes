package net.shuyanmc.mpem.mixin;

import com.google.common.eventbus.EventBus;
import net.shuyanmc.mpem.MpemMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EventBus.class)
public class EventBusMixin {
    @Inject(method = "post",at = @At("HEAD"))
    public void post(Object event, CallbackInfo ci){
        try {
            Class.forName(event.getClass().getName());
        }
        catch (NoClassDefFoundError | ClassNotFoundException e){
            MpemMod.LOGGER.error("Skipped a event.");
            ci.cancel();
        }
    }
}
