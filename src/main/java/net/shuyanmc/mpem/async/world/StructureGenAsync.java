package net.shuyanmc.mpem.async.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@AsyncHandler
public class StructureGenAsync {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<StructureTask> structureQueue = new LinkedBlockingQueue<>();
    // 最大重试次数 (约1秒)
    private static final int MAX_RETRIES = 20;
    // 每tick处理任务上限
    private static final int MAX_TASKS_PER_TICK = 900;
    private static ExecutorService structureExecutor;

    public static void init() {
        structureExecutor = Executors.newFixedThreadPool(CoolConfig.maxthreads.get(), r -> {
            Thread t = new Thread(r, "Async-Structure-Generator");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, ex) ->
                    LOGGER.error("Uncaught exception in structure generator thread", ex));
            return t;
        });
        LOGGER.info("Async Structure Generator initialized");
    }

    public static void shutdown() {
        if (structureExecutor != null) {
            structureExecutor.shutdownNow();
            try {
                if (!structureExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.warn("Structure generator thread pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void placeStructureAsync(ServerLevel level, StructureTemplate template, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            LOGGER.warn("Attempted to place structure at unloaded position {}", pos);
            return;
        }

        StructureTask task = new StructureTask(level, template, pos);
        structureQueue.add(task);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        int processed = 0;
        while (processed < MAX_TASKS_PER_TICK && !structureQueue.isEmpty()) {
            StructureTask task = structureQueue.poll();
            if (task == null) break;

            // 检查重试次数是否超限
            if (task.retryCount().get() >= MAX_RETRIES) {
                LOGGER.error("Structure generation failed after {} retries at {}", MAX_RETRIES, task.pos());
                task.error().set(new TimeoutException("Max retries exceeded"));
                continue;
            }

            structureExecutor.execute(() -> {
                try {
                    // 获取区块坐标
                    int chunkX = task.pos().getX() >> 4;
                    int chunkZ = task.pos().getZ() >> 4;

                    // 双重检查区块加载状态
                    if (!task.level().isAreaLoaded(task.pos(), 1)) {
                        LOGGER.debug("Structure position unloaded during generation: {}", task.pos());
                        retryTask(task);
                        return;
                    }

                    // 关键修复：确保地形已生成
                    if (!isTerrainReady(task.level(), chunkX, chunkZ)) {
                        LOGGER.debug("Terrain not ready for structure at {}, retrying...", task.pos());
                        retryTask(task);
                        return;
                    }

                    // 生成结构
                    task.template().placeInWorld(
                            task.level(),
                            task.pos(),
                            task.pos(),
                            new StructurePlaceSettings(),
                            task.level().random,
                            2
                    );
                    task.completed().set(true);
                    LOGGER.debug("Successfully placed structure at {}", task.pos());
                } catch (Exception e) {
                    LOGGER.error("Failed to place structure asynchronously at {}", task.pos(), e);
                    task.error().set(e);
                }
            });
            processed++;
        }

    }

    // 关键修复：检查地形是否准备就绪
    private static boolean isTerrainReady(ServerLevel level, int chunkX, int chunkZ) {
        try {
            // 检查区块状态是否达到地形装饰阶段
            return level.getChunkSource()
                    .getChunk(chunkX, chunkZ, ChunkStatus.FEATURES, false)
                    .getPersistedStatus()
                    .isOrAfter(ChunkStatus.FEATURES);
        } catch (Exception e) {
            LOGGER.warn("Failed to check terrain status at [{}, {}]", chunkX, chunkZ, e);
            return false;
        }
    }

    // 重试任务处理
    private static void retryTask(StructureTask task) {
        task.retryCount().incrementAndGet();
        // 延迟后重新加入队列
        structureQueue.offer(task);
    }

    private static class StructureTask {
        private final ServerLevel level;
        private final StructureTemplate template;
        private final BlockPos pos;
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicInteger retryCount = new AtomicInteger(0);

        public StructureTask(ServerLevel level, StructureTemplate template, BlockPos pos) {
            this.level = level;
            this.template = template;
            this.pos = pos;
        }

        public ServerLevel level() {
            return level;
        }

        public StructureTemplate template() {
            return template;
        }

        public BlockPos pos() {
            return pos;
        }

        public AtomicBoolean completed() {
            return completed;
        }

        public AtomicReference<Exception> error() {
            return error;
        }

        public AtomicInteger retryCount() {
            return retryCount;
        }
    }
}