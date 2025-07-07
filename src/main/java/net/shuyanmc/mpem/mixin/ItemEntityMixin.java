package net.shuyanmc.mpem.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@AsyncHandler
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Unique
    private int lastMergeTick = -1;

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    public abstract void setItem(ItemStack stack);

    @Shadow
    public abstract void setExtendedLifetime();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        //if (self.level().isClientSide) return;

        // 更新物品堆叠数量显示
        updateStackDisplay(self);

        long gameTime = self.level().getGameTime();
        if (lastMergeTick == -1 || gameTime - lastMergeTick >= 5) {
            lastMergeTick = (int) gameTime;
            tryMergeItems(self);
        }
    }

    @Unique
    private void tryMergeItems(ItemEntity self) {
        double mergeDistance = CoolConfig.mergeDistance.get();
        int configMaxStack = CoolConfig.maxStackSize.get();
        int listMode = CoolConfig.listMode.get();
        List<? extends String> itemList = CoolConfig.itemList.get();

        if (!isMergeAllowed(self.getItem(), listMode, itemList)) return;

        ItemStack stack = self.getItem();
        // 计算最大堆叠数 - 配置为0则无限制，否则使用配置值
        int maxStack = configMaxStack > 0 ? configMaxStack : Integer.MAX_VALUE - 100; // 防止整数溢出

        // 如果当前堆叠数已经达到或超过最大值，则不进行合并
        if (stack.getCount() >= maxStack) return;

        List<ItemEntity> nearby = self.level().getEntitiesOfClass(
                ItemEntity.class,
                self.getBoundingBox().inflate(mergeDistance),
                e -> isValidMergeTarget(self, e, listMode, itemList)
        );

        nearby.sort(Comparator.comparingDouble(self::distanceToSqr));
        int remainingSpace = maxStack - stack.getCount();

        for (ItemEntity other : nearby) {
            if (remainingSpace <= 0) break;

            ItemStack otherStack = other.getItem();
            int transfer = Math.min(otherStack.getCount(), remainingSpace);

            stack.grow(transfer);
            self.setItem(stack);
            self.setExtendedLifetime();

            if (otherStack.getCount() == transfer) {
                other.discard();
            } else {
                otherStack.shrink(transfer);
                other.setItem(otherStack);
                ((ItemEntityMixin) (Object) other).updateStackDisplay(other);
            }

            remainingSpace -= transfer;
        }
    }

    @Unique
    private void updateStackDisplay(ItemEntity entity) {
        if (!CoolConfig.showStackCount.get()) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }

        ItemStack stack = entity.getItem();
        if (stack.getCount() > 1) {
            // 使用深绿色文本显示堆叠数量
            Component countText = Component.literal("x" + stack.getCount())
                    .withStyle(ChatFormatting.DARK_GREEN)
                    .withStyle(ChatFormatting.BOLD);

            entity.setCustomName(countText);
            // 确保名称始终可见
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
    }

    @Unique
    private boolean isValidMergeTarget(ItemEntity self, ItemEntity other, int listMode, List<? extends String> itemList) {
        return self != other &&
                !other.isRemoved() &&
                isSameItem(self.getItem(), other.getItem()) &&
                isMergeAllowed(other.getItem(), listMode, itemList);
    }

    @Unique
    private boolean isSameItem(ItemStack a, ItemStack b) {
        return ItemStack.isSameItem(a, b);
    }

    @Unique
    private boolean isMergeAllowed(ItemStack stack, int listMode, List<? extends String> itemList) {
        if (listMode == 0) return true;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        boolean inList = itemList.contains(id.toString());
        return (listMode == 1) == inList;
    }
}