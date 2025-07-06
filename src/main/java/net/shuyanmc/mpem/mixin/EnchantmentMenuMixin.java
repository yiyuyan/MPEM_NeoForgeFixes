package net.shuyanmc.mpem.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {
    @Redirect(method = "slotsChanged",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchantable()Z"))
    public boolean slot(ItemStack instance){
        return (instance.getItem().getMaxStackSize(instance)==1 && instance.has(DataComponents.MAX_DAMAGE)) || instance.isEnchantable();
    }
}
