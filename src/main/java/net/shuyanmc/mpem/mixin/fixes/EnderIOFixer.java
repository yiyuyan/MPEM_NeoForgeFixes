package net.shuyanmc.mpem.mixin.fixes;

import com.enderio.modconduits.mods.laserio.MekansimIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MekansimIntegration.class)
public class EnderIOFixer {
    @Inject(method = "addEventListener",at = @At("HEAD"),cancellable = true)
    public void add(IEventBus modEventBus, IEventBus forgeEventBus, CallbackInfo ci){
        if(!ModList.get().isLoaded("laserio")) ci.cancel();
    }
}
