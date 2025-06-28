package net.shuyanmc.mpem.optimization;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.config.CoolConfig;

public class MemoryCleaner {
    private long lastCleanTime = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().overworld() == null) return;

        long currentTime = System.currentTimeMillis();
        long interval = CoolConfig.MEMORY_CLEAN_INTERVAL.get() * 1000;

        if (currentTime - lastCleanTime > interval) {
            cleanupResources();
            lastCleanTime = currentTime;
        }
    }

    private void cleanupResources() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 清理纹理内存
        //mc.getTextureManager().tick();

        // 清理区块渲染缓存
        mc.levelRenderer.allChanged();

        // 清理实体缓存
        if (CoolConfig.OPTIMIZE_ENTITY_CLEANUP.get()) {
            for (ServerLevel level : mc.getSingleplayerServer().getAllLevels()) {
                // 修正后的实体清理方式
                level.getAllEntities().forEach(entity -> {
                    if (!entity.isAlive() && entity.tickCount > 600) {
                        entity.discard();
                    }
                });
            }
        }

        // 触发垃圾回收
        if (CoolConfig.ENABLE_GC.get()) {
            System.gc();
        }

        // 调试输出
        if (CoolConfig.DEBUG_LOGGING.get()) {
            System.out.println("[MPEM] 内存清理完成");
        }
    }
}