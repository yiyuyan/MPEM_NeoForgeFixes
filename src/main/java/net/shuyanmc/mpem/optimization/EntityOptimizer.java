package net.shuyanmc.mpem.optimization;

public class EntityOptimizer {
    /*
    private static final Map<Entity, Long> inactiveEntities = new WeakHashMap<>();
    
    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!CoolConfig.disableEntityCollisions.get()) return;
        
        Entity entity = event.getEntity();
        
        // 禁用新生成实体的碰撞
        entity.setNoGravity(true);
        entity.noPhysics = false;
        inactiveEntities.put(entity, System.currentTimeMillis());
    }
    
    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
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
    }*/
}