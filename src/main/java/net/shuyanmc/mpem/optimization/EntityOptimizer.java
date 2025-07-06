package net.shuyanmc.mpem.optimization;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.shuyanmc.mpem.config.CoolConfig;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class EntityOptimizer {
    private static final Map<Entity, Long> inactiveEntities = new WeakHashMap<>();
    
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!CoolConfig.disableEntityCollisions.get()) return;
        
        Entity entity = event.getEntity();
        
        // 禁用新生成实体的碰撞
        entity.setNoGravity(false);
        entity.noPhysics = false;
        inactiveEntities.put(entity, System.currentTimeMillis());
    }
    
    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        inactiveEntities.remove(event.getEntity());
    }
    
    public static void processInactiveEntities() {
        if (!CoolConfig.disableEntityCollisions.get()) return;
        
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Entity, Long>> iterator = inactiveEntities.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Entity, Long> entry = iterator.next();
            Entity entity = entry.getKey();
            
            // 检查实体是否仍然有效
            if (!entity.isAlive()) {
                iterator.remove();
                continue;
            }
            
            // 10秒无活动则冻结
            if (now - entry.getValue() > 10000) {
                entity.setDeltaMovement(Vec3.ZERO);
                entity.setPos(entity.getX(), entity.getY(), entity.getZ());
            }
        }
    }
}