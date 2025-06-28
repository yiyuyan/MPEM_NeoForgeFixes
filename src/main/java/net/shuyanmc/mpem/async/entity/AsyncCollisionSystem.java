package net.shuyanmc.mpem.async.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@AsyncHandler
public class AsyncCollisionSystem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<CollisionTask> collisionQueue = new LinkedBlockingQueue<>();
    private static final Map<UUID, CollisionTask> activeTasks = new ConcurrentHashMap<>();
    private static ExecutorService collisionExecutor;

    public static void init() {
        collisionExecutor = Executors.newFixedThreadPool(CoolConfig.maxthreads.get(), r -> {
            Thread t = new Thread(r, "Async-Collision-Checker");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, ex) ->
                    LOGGER.error("Uncaught exception in collision thread", ex));
            return t;
        });
        LOGGER.info("Async Collision System initialized");
    }

    public static void shutdown() {
        if (collisionExecutor != null) {
            collisionExecutor.shutdownNow();
            try {
                if (!collisionExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.warn("Collision thread pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void checkCollisionsAsync(Collection<Entity> entities) {
        if (entities.isEmpty()) return;

        CollisionTask task = new CollisionTask(new ArrayList<>(entities));
        collisionQueue.add(task);
        activeTasks.put(task.taskId(), task);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        // 处理最多20个碰撞任务
        for (int i = 0; i < 200 && !collisionQueue.isEmpty(); i++) {
            CollisionTask task = collisionQueue.poll();
            if (task != null && activeTasks.containsKey(task.taskId())) {
                collisionExecutor.execute(() -> {
                    try {
                        List<CollisionResult> results = new ArrayList<>();
                        Entity[] entityArray = task.entities().toArray(new Entity[0]);

                        for (int j = 0; j < entityArray.length; j++) {
                            Entity e1 = entityArray[j];
                            if (e1.isRemoved() || !e1.isAlive()) continue;

                            for (int k = j + 1; k < entityArray.length; k++) {
                                Entity e2 = entityArray[k];
                                if (e2.isRemoved() || !e2.isAlive()) continue;

                                if (e1.getBoundingBox().intersects(e2.getBoundingBox())) {
                                    results.add(new CollisionResult(e1, e2));
                                }
                            }
                        }

                        task.results().addAll(results);
                        task.completed().set(true);
                    } catch (Exception e) {
                        LOGGER.error("Collision detection failed", e);
                        task.error().set(e);
                    }
                });
            }
        }

        // 应用碰撞结果
        activeTasks.entrySet().removeIf(entry -> {
            CollisionTask task = entry.getValue();
            if (task.completed().get()) {
                if (task.error().get() == null) {
                    for (CollisionResult result : task.results()) {
                        if (!result.entity1().isRemoved() && result.entity1().isAlive() &&
                                !result.entity2().isRemoved() && result.entity2().isAlive()) {

                            // 在主线程安全地处理碰撞
                            result.entity1().push(result.entity2());
                            result.entity2().push(result.entity1());
                        }
                    }
                    LOGGER.debug("Applied {} collision results", task.results().size());
                }
                return true;
            }
            return false;
        });
    }


    private static class CollisionTask {
        private final UUID taskId = UUID.randomUUID();
        private final List<Entity> entities;
        private final List<CollisionResult> results = new CopyOnWriteArrayList<>();
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final AtomicReference<Exception> error = new AtomicReference<>();

        public CollisionTask(List<Entity> entities) {
            this.entities = Collections.unmodifiableList(entities);
        }

        public UUID taskId() {
            return taskId;
        }

        public List<Entity> entities() {
            return entities;
        }

        public List<CollisionResult> results() {
            return results;
        }

        public AtomicBoolean completed() {
            return completed;
        }

        public AtomicReference<Exception> error() {
            return error;
        }
    }

    private record CollisionResult(Entity entity1, Entity entity2) {
    }
}