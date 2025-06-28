package net.shuyanmc.mpem.async.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@AsyncHandler
public class AsyncAIManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<PathfinderMob, PathTask> pathfindingTasks = new ConcurrentHashMap<>();
    private static ExecutorService aiExecutor;

    public static void init() {
        int threads = Math.max(CoolConfig.maxthreads.get(), Runtime.getRuntime().availableProcessors() / 2);
        aiExecutor = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "Async-AI-Processor");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, ex) ->
                    LOGGER.error("Uncaught exception in AI thread", ex));
            return t;
        });
        LOGGER.info("Async AI Manager initialized with {} threads", threads);
    }

    public static void shutdown() {
        if (aiExecutor != null) {
            aiExecutor.shutdownNow();
            try {
                if (!aiExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.warn("AI thread pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void computePathAsync(PathfinderMob mob, BlockPos target) {
        if (!mob.level().isLoaded(target)) {
            LOGGER.warn("Attempted to pathfind to unloaded position: {}", target);
            return;
        }

        if (pathfindingTasks.containsKey(mob)) {
            LOGGER.debug("Pathfinding already in progress for {}", mob.getName().getString());
            return;
        }

        PathTask task = new PathTask(mob, target);
        pathfindingTasks.put(mob, task);
        aiExecutor.execute(() -> {
            try {
                if (!mob.isAlive() || mob.isRemoved()) {
                    LOGGER.debug("Entity removed during pathfinding: {}", mob.getName().getString());
                    return;
                }

                PathNavigation navigation = mob.getNavigation();
                Path path = navigation.createPath(target, 0);
                task.path().set(path);
            } catch (Exception e) {
                LOGGER.error("Pathfinding failed for {}", mob.getName().getString(), e);
                task.error().set(e);
            }
        });
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        pathfindingTasks.entrySet().removeIf(entry -> {
            PathfinderMob mob = entry.getKey();
            PathTask task = entry.getValue();

            if (!mob.isAlive() || mob.isRemoved()) {
                LOGGER.debug("Removing pathfinding task for dead/removed entity");
                return true;
            }

            if (task.path().get() != null || task.error().get() != null) {
                if (task.path().get() != null) {
                    mob.getNavigation().moveTo(task.path().get(), 1.0);
                    LOGGER.debug("Path applied for {}", mob.getName().getString());
                }
                return true;
            }

            return false;
        });

    }

    private static class PathTask {
        private final PathfinderMob mob;
        private final BlockPos target;
        private final AtomicReference<Path> path = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();

        public PathTask(PathfinderMob mob, BlockPos target) {
            this.mob = mob;
            this.target = target;
        }

        public PathfinderMob mob() {
            return mob;
        }

        public BlockPos target() {
            return target;
        }

        public AtomicReference<Path> path() {
            return path;
        }

        public AtomicReference<Exception> error() {
            return error;
        }
    }
}