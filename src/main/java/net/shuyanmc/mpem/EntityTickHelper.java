package net.shuyanmc.mpem;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.shuyanmc.mpem.api.IOptimizableEntity;
import net.shuyanmc.mpem.config.CoolConfig;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class EntityTickHelper {
    // 使用parse()代替new ResourceLocation()
    private static final Supplier<Set<Item>> ITEM_WHITELIST_CACHE = Suppliers.memoize(() ->
            CoolConfig.itemWhitelist.get().stream()
                    .map(s -> ResourceLocation.parse(s))
                    .map(loc -> BuiltInRegistries.ITEM.get(loc))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
    );

    /**
     * 判断实体是否需要取消Tick
     */
    public static boolean shouldCancelTick(Entity entity) {
        // 基础检查
        if (entity == null || entity.level() == null || !CoolConfig.optimizeEntities.get()) {
            return false;
        }

        // 白名单实体直接放行
        if (isAlwaysTicking(entity)) {
            return false;
        }

        // 死亡实体特殊处理
        if (isDeadEntity(entity)) {
            return false;
        }

        // 物品实体特殊逻辑
        if (entity instanceof ItemEntity itemEntity) {
            return shouldOptimizeItemEntity(itemEntity);
        }

        BlockPos pos = entity.blockPosition();
        Level level = entity.level();

        // 袭击事件特殊处理
        if (shouldBypassForRaid(entity, level, pos)) {
            return false;
        }

        // 最终玩家距离检查
        return !isPlayerNearby(level, pos);
    }

    // ========== 私有工具方法 ==========

    private static boolean isAlwaysTicking(Entity entity) {
        return ((IOptimizableEntity) entity.getType()).shouldAlwaysTick();
    }

    private static boolean isDeadEntity(Entity entity) {
        return !CoolConfig.ignoreDeadEntities.get() &&
                entity instanceof LivingEntity living &&
                living.isDeadOrDying();
    }

    private static boolean shouldOptimizeItemEntity(ItemEntity itemEntity) {
        return CoolConfig.optimizeItems.get() &&
                !ITEM_WHITELIST_CACHE.get().contains(itemEntity.getItem().getItem());
    }

    private static boolean shouldBypassForRaid(Entity entity, Level level, BlockPos pos) {
        return CoolConfig.tickRaidersInRaid.get() &&
                level instanceof ServerLevel serverLevel &&
                serverLevel.getRaidAt(pos) != null &&
                (entity instanceof Raider ||
                        ((IOptimizableEntity) entity.getType()).shouldTickInRaid());
    }

    private static boolean isPlayerNearby(Level level, BlockPos pos) {
        int horizontalRange = CoolConfig.horizontalRange.get();
        int verticalRange = CoolConfig.verticalRange.get();
        double rangeSq = horizontalRange * horizontalRange;

        AABB checkArea = new AABB(
                pos.getX() - horizontalRange,
                pos.getY() - verticalRange,
                pos.getZ() - horizontalRange,
                pos.getX() + horizontalRange,
                pos.getY() + verticalRange,
                pos.getZ() + horizontalRange
        );

        return !level.getEntitiesOfClass(
                Player.class,
                checkArea,
                player -> player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < rangeSq
        ).isEmpty();
    }
}