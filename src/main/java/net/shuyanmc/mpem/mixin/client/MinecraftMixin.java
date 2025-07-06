// MinecraftMixin.java - 无变化
package net.shuyanmc.mpem.mixin.client;

import net.shuyanmc.mpem.*;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> cir) {
        Minecraft instance = Minecraft.getInstance();
        if (!instance.isWindowActive()) {
            cir.setReturnValue(FrameRateController.getInactiveFrameRateLimit());
        }
    }
}