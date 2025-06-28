// net/shuyanmc/mpem/mixin/ItemStackMixin.java
package net.shuyanmc.mpem.mixin;

import net.minecraft.world.item.ItemStack;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@AsyncHandler
@Mixin(ItemStack.class)
public abstract class ItemCC {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void overrideMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        if (CoolConfig.ENABLED.get()) {
            cir.setReturnValue(CoolConfig.MAX_STACK_SIZE.get());
        }
    }
}