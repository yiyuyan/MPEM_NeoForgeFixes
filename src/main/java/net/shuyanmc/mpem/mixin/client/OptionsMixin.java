// OptionsMixin.java - 无变化
package net.shuyanmc.mpem.mixin.client;

import net.shuyanmc.mpem.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "getEffectiveRenderDistance", at = @At("HEAD"), cancellable = true)
    private void onGetEffectiveRenderDistance(CallbackInfoReturnable<Integer> cir) {
        Minecraft instance = Minecraft.getInstance();
        if (!instance.isWindowActive()) {
            cir.setReturnValue(RenderDistanceController.getInactiveRenderDistance());
        }
    }
}