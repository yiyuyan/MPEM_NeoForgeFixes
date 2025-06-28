package net.shuyanmc.mpem.mixin;

import net.minecraft.world.item.ItemStack;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@AsyncHandler
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract int getMaxStackSize();

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void onGetMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        int configMax = CoolConfig.maxStackSize.get();
        if (configMax > 0) {
            int vanillaMax = this.getMaxStackSize();
            cir.setReturnValue(Math.min(configMax, vanillaMax));
        }
    }

    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void onIsStackable(CallbackInfoReturnable<Boolean> cir) {
        int configMax = CoolConfig.maxStackSize.get();
        if (configMax == 0) {
            cir.setReturnValue(this.getMaxStackSize() > 1);
        }
    }
}