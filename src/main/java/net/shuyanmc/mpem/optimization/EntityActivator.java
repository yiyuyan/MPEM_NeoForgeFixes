package net.shuyanmc.mpem.optimization;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;

public class EntityActivator {
    private static final Map<Entity, Boolean> activeEntities = new HashMap<>();

    public static boolean isEntityActive(Entity entity) {
        if (!activeEntities.containsKey(entity)) {
            updateEntityActivity(entity);
        }
        return activeEntities.get(entity);
    }

    private static void updateEntityActivity(Entity entity) {
        boolean active = false;

        // 检查附近玩家
        for (Player player : entity.level().players()) {
            if (player.distanceToSqr(entity) < 1024) { // 32^2
                active = true;
                break;
            }
        }

        // 检查是否在玩家视野内
        if (!active) {
            active = entity.level().getNearestPlayer(entity, 64) != null;
        }

        activeEntities.put(entity, active);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {

        // 每10秒清理一次缓存
        if (event.getServer().getTickCount() % 200 == 0) {
            activeEntities.keySet().removeIf(e -> !e.isAlive());
        }
    }
}