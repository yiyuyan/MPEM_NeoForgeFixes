package net.shuyanmc.mpem.async.player;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.AsyncHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@AsyncHandler
public class AsyncPlayerData {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<SaveTask> saveQueue = new LinkedBlockingQueue<>();
    private static final ConcurrentMap<UUID, LoadTask> loadTasks = new ConcurrentHashMap<>();
    private static ExecutorService ioExecutor;

    public static void init() {
        ioExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Async-PlayerData-IO");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, ex) ->
                    LOGGER.error("Uncaught exception in player data thread", ex));
            return t;
        });
        LOGGER.info("Async Player Data Manager initialized");
    }

    public static void shutdown() {
        if (ioExecutor != null) {
            ioExecutor.shutdownNow();
            try {
                if (!ioExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.warn("Player data thread pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void savePlayerDataAsync(ServerPlayer player, Path dataDir) {
        SaveTask task = new SaveTask(player, dataDir);
        saveQueue.add(task);
    }

    public static CompletableFuture<PlayerData> loadPlayerDataAsync(UUID playerId, Path dataDir) {
        if (loadTasks.containsKey(playerId)) {
            LOGGER.warn("Player data load already in progress for {}", playerId);
            return CompletableFuture.failedFuture(new IllegalStateException("Load already in progress"));
        }

        LoadTask task = new LoadTask(playerId, dataDir);
        loadTasks.put(playerId, task);

        ioExecutor.execute(() -> {
            try {
                Path playerFile = dataDir.resolve(playerId.toString() + ".dat");
                if (Files.exists(playerFile)) {
                    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(playerFile))) {
                        PlayerData data = (PlayerData) ois.readObject();
                        task.result().set(data);
                    }
                } else {
                    task.result().set(null);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load player data for {}", playerId, e);
                task.error().set(e);
            } finally {
                task.completed().set(true);
            }
        });

        return task.future();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        // 处理保存任务
        SaveTask saveTask;
        while ((saveTask = saveQueue.poll()) != null) {
            final SaveTask finalSaveTask = saveTask; // 创建final副本
            ioExecutor.execute(() -> {
                try {
                    ServerPlayer player = finalSaveTask.player();
                    if (player == null || player.isRemoved()) {
                        LOGGER.warn("Player not available for data save");
                        return;
                    }

                    Path playerFile = finalSaveTask.dataDir().resolve(player.getUUID().toString() + ".dat");
                    try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(playerFile))) {
                        PlayerData data = new PlayerData(player);
                        oos.writeObject(data);
                        LOGGER.info("Player data saved for {}", player.getName().getString());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to save player data", e);
                }
            });
        }

        // 处理加载任务结果
        loadTasks.entrySet().removeIf(entry -> {
            LoadTask task = entry.getValue();
            if (task.completed().get()) {
                if (task.error().get() != null) {
                    task.future().completeExceptionally(task.error().get());
                } else {
                    task.future().complete(task.result().get());
                }
                return true;
            }
            return false;
        });

    }

    public static class PlayerData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final UUID playerId;
        private final String playerName;

        public PlayerData(ServerPlayer player) {
            this.playerId = player.getUUID();
            this.playerName = player.getName().getString();
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    private static class SaveTask {
        private final ServerPlayer player;
        private final Path dataDir;

        public SaveTask(ServerPlayer player, Path dataDir) {
            this.player = player;
            this.dataDir = dataDir;
        }

        public ServerPlayer player() {
            return player;
        }

        public Path dataDir() {
            return dataDir;
        }
    }

    private static class LoadTask {
        private final UUID playerId;
        private final Path dataDir;
        private final CompletableFuture<PlayerData> future = new CompletableFuture<>();
        private final AtomicReference<PlayerData> result = new AtomicReference<>();
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final AtomicReference<Exception> error = new AtomicReference<>();

        public LoadTask(UUID playerId, Path dataDir) {
            this.playerId = playerId;
            this.dataDir = dataDir;
        }

        public CompletableFuture<PlayerData> future() {
            return future;
        }

        public AtomicReference<PlayerData> result() {
            return result;
        }

        public AtomicBoolean completed() {
            return completed;
        }

        public AtomicReference<Exception> error() {
            return error;
        }
    }
}