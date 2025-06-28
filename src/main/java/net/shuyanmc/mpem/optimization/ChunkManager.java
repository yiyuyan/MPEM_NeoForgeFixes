package net.shuyanmc.mpem.optimization;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.config.CoolConfig;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private static final Map<ChunkPos, Long> chunkAccessTimes = new HashMap<>();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (!CoolConfig.aggressiveChunkUnloading.get()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long unloadThreshold = CoolConfig.chunkUnloadDelay.get() * 1000L;

        // 更新所有活跃区块
        for (ServerLevel level : event.getServer().getAllLevels()) {
            level.getPlayers(player -> {
                ChunkPos pos = new ChunkPos(player.chunkPosition().x, player.chunkPosition().z);
                chunkAccessTimes.put(pos, currentTime);
                return false;
            });
        }

        // 卸载非活跃区块
        chunkAccessTimes.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > unloadThreshold) {
                for (ServerLevel level : event.getServer().getAllLevels()) {
                    if (level.getChunkSource().hasChunk(entry.getKey().x, entry.getKey().z)) {
                        // 使用正确的区块卸载方法
                        level.getChunkSource().tick(() -> true, true);
                    }
                }
                return true;
            }
            return false;
        });
    }
}