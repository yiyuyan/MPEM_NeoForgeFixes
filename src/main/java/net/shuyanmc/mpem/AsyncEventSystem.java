package net.shuyanmc.mpem;


import net.neoforged.bus.api.Event;
import net.neoforged.fml.loading.FMLEnvironment;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class AsyncEventSystem {
    public static final Logger LOGGER = LogManager.getLogger("MPEM-Async");
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.min(4, Math.max(CoolConfig.maxCPUPro.get(), CPU_CORES));
    private static final int MAX_POOL_SIZE = Math.min(16, CPU_CORES * 2);
    private static final ThreadPoolExecutor ASYNC_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new AsyncEventThreadFactory(),
            new AsyncEventRejectedHandler()
    );
    private static final ConcurrentMap<Class<? extends Event>, EventTypeInfo> EVENT_TYPE_INFOS = new ConcurrentHashMap<>();
    private static final AtomicLong totalAsyncTasks = new AtomicLong(0);
    private static final AtomicLong failedAsyncTasks = new AtomicLong(0);
    private static volatile boolean initialized = false;

    static {
        ASYNC_EXECUTOR.prestartAllCoreThreads();
    }

    public static void initialize() {
        if (!initialized) {
            registerCommonAsyncEvents();
            initialized = true;
            LOGGER.info("Async Event System initialized. Core: {}, Max: {}", CORE_POOL_SIZE, MAX_POOL_SIZE);
        }
    }

    private static void registerCommonAsyncEvents() {
        String[] asyncEvents = {
                "net.minecraftforge.event.entity.player.PlayerEvent",
                "net.minecraftforge.event.entity.player.AdvancementEvent",
                "net.minecraftforge.event.entity.player.AnvilRepairEvent",
                "net.minecraftforge.event.entity.player.PlayerInteractEvent",
                "net.minecraftforge.event.entity.player.PlayerXpEvent",
                "net.minecraftforge.event.level.BlockEvent",
                "net.minecraftforge.event.level.ChunkEvent",
                "net.minecraftforge.event.level.ExplosionEvent",
                "net.minecraftforge.event.entity.EntityEvent",
                "net.minecraftforge.event.entity.EntityJoinLevelEvent",
                "net.minecraftforge.event.entity.EntityLeaveLevelEvent",
                "net.minecraftforge.event.entity.EntityMountEvent",
                "net.minecraftforge.event.entity.EntityTeleportEvent",
                "net.minecraftforge.event.entity.item.ItemEvent",
                "net.minecraftforge.event.entity.item.ItemExpireEvent",
                "net.minecraftforge.event.entity.item.ItemTossEvent",
                "net.minecraftforge.event.level.LevelEvent",
                "net.minecraftforge.event.level.BlockEvent",
                "net.minecraftforge.event.level.ChunkEvent",
                "net.minecraftforge.event.network.CustomPayloadEvent",
                "net.minecraftforge.event.network.NetworkEvent",
                "net.minecraftforge.event.CommandEvent",
                "net.minecraftforge.event.TagsUpdatedEvent",
                "net.minecraftforge.event.LootTableLoadEvent",
                "net.minecraftforge.event.RegisterCommandsEvent"
        };

        String[] syncEvents = {
                "net.minecraftforge.event.TickEvent",
                "net.minecraftforge.event.level.LevelTickEvent",
                "net.minecraftforge.event.entity.living.LivingEvent",
                "net.minecraftforge.event.entity.living.LivingAttackEvent",
                "net.minecraftforge.event.entity.living.LivingDamageEvent",
                "net.minecraftforge.event.entity.living.LivingDeathEvent",
                "net.minecraftforge.event.entity.living.LivingDropsEvent",
                "net.minecraftforge.event.entity.living.LivingExperienceDropEvent",
                "net.minecraftforge.event.entity.living.LivingHealEvent",
                "net.minecraftforge.event.entity.living.LivingKnockBackEvent",
                "net.minecraftforge.event.server.ServerStartingEvent",
                "net.minecraftforge.event.server.ServerStoppingEvent",
                "net.minecraftforge.event.server.ServerStartedEvent"
        };

        for (String className : asyncEvents) {
            try {
                Class<? extends Event> eventClass = loadClass(className);
                if(eventClass==null){
                    LOGGER.warn("Can't load the class {}",className);
                    continue;
                }
                if (isClientOnlyEvent(eventClass)) {
                    LOGGER.debug("Skipping client event: {}", className);
                    continue;
                }
                registerAsyncEvent(eventClass);
            } catch (ClassNotFoundException e) {
                LOGGER.warn("[Fallback] Failed to load async event: {}, falling back to SYNC", className);
                try {
                    Class<? extends Event> eventClass = loadClass(className);
                    if(eventClass==null){
                        LOGGER.warn("Can't load the class {}",className);
                        continue;
                    }
                    registerSyncEvent(eventClass);
                } catch (ClassNotFoundException ex) {
                    LOGGER.error("[Critical] Event class not found: {}", className);
                }
            }
        }

        for (String className : syncEvents) {
            try {
                Class<? extends Event> eventClass = loadClass(className);
                if(eventClass==null){
                    LOGGER.warn("Can't load the class {}",className);
                    continue;
                }
                registerSyncEvent(eventClass);
            } catch (ClassNotFoundException e) {
                LOGGER.error("[Critical] Sync event class not found: {}", className);
            }
        }

        LOGGER.info("Registered {} async event types", EVENT_TYPE_INFOS.size());
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Event> loadClass(String className) throws ClassNotFoundException {
        try {
            Class<?> clazz = Class.forName(className);
            if (!Event.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + className + " does not extend Event");
            }
            return (Class<? extends Event>) clazz;
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            //MpemMod.LOGGER.error(e.toString());
            try {
                ClassLoader forgeLoader = Event.class.getClassLoader();
                Class<?> clazz = Class.forName(className, true, forgeLoader);
                return (Class<? extends Event>) clazz;
            } catch (NoClassDefFoundError | ClassNotFoundException ex) {
                //MpemMod.LOGGER.error(e.toString());
                return null;
            }
        }
    }

    // 检测是否为客户端专有事件
    public static boolean isClientOnlyEvent(Class<? extends Event> eventClass) {
        return eventClass.getName().startsWith("client");
    }

    public static void registerAsyncEvent(Class<? extends Event> eventType) {
        EVENT_TYPE_INFOS.compute(eventType, (k, v) -> {
            if (v == null) {
                EventTypeInfo info = new EventTypeInfo(true);
                info.isClientEvent = isClientOnlyEvent(eventType);
                return info;
            }
            v.async = true;
            v.healthy = true;
            v.failedCount.set(0);
            v.isClientEvent = isClientOnlyEvent(eventType);
            return v;
        });
        LOGGER.debug("Registered async event: {}", eventType.getName());
    }

    public static void registerSyncEvent(Class<? extends Event> eventType) {
        EVENT_TYPE_INFOS.compute(eventType, (k, v) -> {
            if (v == null) {
                EventTypeInfo info = new EventTypeInfo(false);
                info.isClientEvent = isClientOnlyEvent(eventType);
                return info;
            }
            v.async = false;
            v.isClientEvent = isClientOnlyEvent(eventType);
            return v;
        });
        LOGGER.debug("Registered sync event: {}", eventType.getName());
    }

    public static boolean shouldHandleAsync(Class<? extends Event> eventType) {
        EventTypeInfo info = EVENT_TYPE_INFOS.get(eventType);
        if (info != null) {
            // 如果是客户端事件且在服务器环境，禁用异步
            if (info.isClientEvent && FMLEnvironment.dist.isDedicatedServer()) {
                return false;
            }
            return info.async && info.healthy;
        }
        return eventType.getSimpleName().contains("Async");
    }

    public static CompletableFuture<Void> executeAsync(Class<? extends Event> eventType, Runnable task) {
        totalAsyncTasks.incrementAndGet();
        if (!initialized) {
            LOGGER.warn("[Fallback] Async system not initialized, executing immediately");
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        EventTypeInfo info = EVENT_TYPE_INFOS.computeIfAbsent(
                eventType,
                k -> new EventTypeInfo(shouldHandleAsync(eventType))
        );

        // 如果是客户端事件但在服务器环境，直接同步执行
        if (info.isClientEvent && FMLEnvironment.dist.isDedicatedServer()) {
            LOGGER.warn("Attempted to execute client event on server: {}", eventType.getName());
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        if (!info.async || !info.healthy) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        info.pendingTasks.incrementAndGet();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

        return CompletableFuture.runAsync(() -> {
            Thread.currentThread().setContextClassLoader(contextLoader);
            try {
                task.run();
            } catch (Throwable t) {
                failedAsyncTasks.incrementAndGet();
                info.failedCount.incrementAndGet();
                LOGGER.error("Async task for event {} failed", eventType.getSimpleName(), t);
                if (CoolConfig.DISABLE_ASYNC_ON_ERROR.get() || info.failedCount.get() >= 3) {
                    info.healthy = false;
                    LOGGER.warn("Disabled async processing for event type {} due to repeated failures", eventType.getSimpleName());
                }
                throw t;
            } finally {
                info.pendingTasks.decrementAndGet();
                Thread.currentThread().setContextClassLoader(contextLoader);
            }
        }, ASYNC_EXECUTOR).exceptionally(ex -> {
            LOGGER.warn("Retrying event {} synchronously after async failure", eventType.getSimpleName());
            task.run();
            return null;
        });
    }

    public static void shutdown() {
        if (!initialized) return;

        LOGGER.info("Shutting down async event system. Total tasks: {}, Failed: {}",
                totalAsyncTasks.get(), failedAsyncTasks.get());

        ASYNC_EXECUTOR.shutdown();
        try {
            if (!ASYNC_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                LOGGER.warn("Forcing async event executor shutdown");
                ASYNC_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            ASYNC_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static int getQueueSize() {
        return ASYNC_EXECUTOR.getQueue().size();
    }

    public static int getActiveCount() {
        return ASYNC_EXECUTOR.getActiveCount();
    }

    public static int getPoolSize() {
        return ASYNC_EXECUTOR.getPoolSize();
    }

    public static int getMaxPoolSize() {
        return MAX_POOL_SIZE;
    }

    public static int getAsyncEventCount() {
        return EVENT_TYPE_INFOS.size();
    }

    public static void tryRegisterAsyncEvent(Consumer<?> consumer) {
        try {
            for (Type type : consumer.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) type;
                    if (paramType.getRawType().equals(Consumer.class)) {
                        Type[] typeArgs = paramType.getActualTypeArguments();
                        if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                            Class<?> eventClass = (Class<?>) typeArgs[0];
                            if (Event.class.isAssignableFrom(eventClass)) {
                                @SuppressWarnings("unchecked")
                                Class<? extends Event> eventType = (Class<? extends Event>) eventClass;
                                registerAsyncEvent(eventType);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to determine event type for consumer: {}", consumer.getClass().getName(), e);
        }
    }

    public static void resetEventTypeHealth(Class<? extends Event> eventType) {
        EVENT_TYPE_INFOS.computeIfPresent(eventType, (k, v) -> {
            v.healthy = true;
            v.failedCount.set(0);
            return v;
        });
    }

    private static class EventTypeInfo {
        final AtomicInteger pendingTasks = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);
        volatile boolean async;
        volatile boolean healthy = true;
        volatile boolean isClientEvent = false;

        EventTypeInfo(boolean async) {
            this.async = async;
        }

        boolean shouldRetryAsync() {
            return async && healthy && failedCount.get() < 3;
        }
    }

    private static class AsyncEventThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "MPEM-Async-Worker-" + counter.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setUncaughtExceptionHandler((t, e) -> {
                LOGGER.error("Uncaught exception in async event thread", e);
            });
            return thread;
        }
    }

    private static class AsyncEventRejectedHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOGGER.warn("[Fallback] Async queue overflow ({} tasks), executing immediately", executor.getQueue().size());
            if (!executor.isShutdown()) {
                try {
                    r.run();
                } catch (Throwable t) {
                    LOGGER.error("Fallback execution failed", t);
                }
            }
        }
    }
}