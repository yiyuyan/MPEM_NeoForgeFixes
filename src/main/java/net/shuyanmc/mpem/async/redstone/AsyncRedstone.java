package net.shuyanmc.mpem.async.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.AsyncHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@AsyncHandler
public class AsyncRedstone {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<RedstoneTask> redstoneQueue = new LinkedBlockingQueue<>();
    private static final Map<BlockPos, RedstoneSnapshot> activeTasks = new ConcurrentHashMap<>();
    private static ExecutorService redstoneExecutor;

    public static void init() {
        redstoneExecutor = Executors.newWorkStealingPool();
        LOGGER.info("Async Redstone System initialized with work-stealing pool");
    }

    public static void shutdown() {
        if (redstoneExecutor != null) {
            redstoneExecutor.shutdownNow();
            try {
                if (!redstoneExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.warn("Redstone thread pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void computeRedstoneAsync(ServerLevel level, BlockPos sourcePos) {
        if (!level.isLoaded(sourcePos)) {
            LOGGER.warn("Attempted redstone computation at unloaded position {}", sourcePos);
            return;
        }

        if (activeTasks.containsKey(sourcePos)) {
            LOGGER.debug("Redstone computation already in progress for {}", sourcePos);
            return;
        }

        // 创建线程安全的快照
        RedstoneSnapshot snapshot = new RedstoneSnapshot(level, sourcePos);
        if (!snapshot.isValid()) {
            LOGGER.warn("Failed to create snapshot for redstone computation at {}", sourcePos);
            return;
        }

        RedstoneTask task = new RedstoneTask(snapshot);
        redstoneQueue.add(task);
        activeTasks.put(sourcePos, snapshot);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        // 处理红石计算任务
        RedstoneTask currentTask;
        while ((currentTask = redstoneQueue.poll()) != null) {
            final RedstoneTask finalTask = currentTask;
            redstoneExecutor.execute(() -> {
                try {
                    if (!finalTask.snapshot().isStillValid()) {
                        LOGGER.debug("Redstone snapshot invalidated during computation");
                        return;
                    }

                    Map<BlockPos, Integer> powerLevels = new HashMap<>();
                    Deque<BlockPos> queue = new ArrayDeque<>();
                    queue.add(finalTask.snapshot().sourcePos());

                    while (!queue.isEmpty()) {
                        BlockPos current = queue.poll();

                        // 使用快照数据而不是实时世界状态
                        int power = calculatePower(finalTask.snapshot(), current);
                        powerLevels.put(current, power);
                        finalTask.results().add(new RedstoneResult(current, power));

                        for (BlockPos neighbor : getNeighbors(current)) {
                            if (!powerLevels.containsKey(neighbor) &&
                                    finalTask.snapshot().containsPosition(neighbor)) {
                                queue.add(neighbor);
                            }
                        }
                    }

                    finalTask.completed().set(true);
                } catch (Exception e) {
                    LOGGER.error("Redstone computation failed", e);
                    finalTask.error().set(e);
                }
            });
        }

        // 应用红石计算结果
        Iterator<Map.Entry<BlockPos, RedstoneSnapshot>> it = activeTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, RedstoneSnapshot> entry = it.next();
            RedstoneSnapshot snapshot = entry.getValue();

            for (RedstoneTask task : snapshot.assignedTasks()) {
                if (task.completed().get()) {
                    if (task.error().get() == null) {
                        // 应用更新前验证快照有效性
                        if (snapshot.isStillValid()) {
                            for (RedstoneResult result : task.results()) {
                                BlockPos pos = result.pos();
                                if (snapshot.level().isLoaded(pos)) {
                                    snapshot.level().updateNeighborsAt(pos,
                                            snapshot.level().getBlockState(pos).getBlock());
                                }
                            }
                            LOGGER.debug("Applied {} redstone updates", task.results().size());
                        } else {
                            LOGGER.debug("Skipping redstone update - snapshot invalidated");
                        }
                    }
                    it.remove();
                    break;
                }
            }
        }

    }

    // 使用快照数据计算红石能量
    private static int calculatePower(RedstoneSnapshot snapshot, BlockPos pos) {
        return snapshot.getPowerAt(pos);
    }

    private static List<BlockPos> getNeighbors(BlockPos pos) {
        return List.of(
                pos.above(), pos.below(),
                pos.north(), pos.south(),
                pos.east(), pos.west()
        );
    }

    // 线程安全的红石快照
    private static class RedstoneSnapshot {
        private final ServerLevel level;
        private final BlockPos sourcePos;
        private final Map<BlockPos, BlockState> stateMap = new ConcurrentHashMap<>();
        private final Set<RedstoneTask> tasks = ConcurrentHashMap.newKeySet();
        private final long creationTime;

        public RedstoneSnapshot(ServerLevel level, BlockPos sourcePos) {
            this.level = level;
            this.sourcePos = sourcePos;
            this.creationTime = System.nanoTime();

            // 创建半径10格范围内的快照
            int radius = 10;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = sourcePos.offset(x, y, z);
                        if (level.isLoaded(pos)) {
                            stateMap.put(pos, level.getBlockState(pos));
                        }
                    }
                }
            }
        }

        public boolean isValid() {
            return !stateMap.isEmpty();
        }

        public boolean isStillValid() {
            // 快照在创建后100ms内有效
            return System.nanoTime() - creationTime < TimeUnit.MILLISECONDS.toNanos(100);
        }

        public boolean containsPosition(BlockPos pos) {
            return stateMap.containsKey(pos);
        }

        public int getPowerAt(BlockPos pos) {
            BlockState state = stateMap.get(pos);
            return state != null ? state.getSignal(level, pos, null) : 0;
        }

        public void assignTask(RedstoneTask task) {
            tasks.add(task);
        }

        public Set<RedstoneTask> assignedTasks() {
            return tasks;
        }

        public ServerLevel level() {
            return level;
        }

        public BlockPos sourcePos() {
            return sourcePos;
        }
    }

    private static class RedstoneTask {
        private final RedstoneSnapshot snapshot;
        private final List<RedstoneResult> results = new CopyOnWriteArrayList<>();
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final AtomicReference<Exception> error = new AtomicReference<>();

        public RedstoneTask(RedstoneSnapshot snapshot) {
            this.snapshot = snapshot;
            snapshot.assignTask(this);
        }

        public RedstoneSnapshot snapshot() {
            return snapshot;
        }

        public List<RedstoneResult> results() {
            return results;
        }

        public AtomicBoolean completed() {
            return completed;
        }

        public AtomicReference<Exception> error() {
            return error;
        }
    }

    private record RedstoneResult(BlockPos pos, int power) {
    }
}