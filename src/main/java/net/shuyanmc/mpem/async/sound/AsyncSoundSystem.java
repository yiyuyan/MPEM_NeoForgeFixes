package net.shuyanmc.mpem.async.sound;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@AsyncHandler
public class AsyncSoundSystem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<SoundTask> soundQueue = new LinkedBlockingQueue<>();
    private static ExecutorService soundExecutor;

    public static void init() {
        soundExecutor = Executors.newFixedThreadPool(CoolConfig.maxthreads.get(), r -> {
            Thread t = new Thread(r, "Async-Sound-Processor");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, ex) ->
                    LOGGER.error("Uncaught exception in sound thread", ex));
            return t;
        });
        LOGGER.info("Async Sound System initialized");
    }

    public static void shutdown() {
        if (soundExecutor != null) {
            soundExecutor.shutdownNow();
            try {
                if (!soundExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.warn("Sound thread pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void playSoundIfInRangeAsync(ServerPlayer player, SoundEvent sound,
                                               SoundSource category, Vec3 pos,
                                               float volume, float pitch) {
        if (player.isRemoved() || !player.isAlive()) {
            LOGGER.warn("Attempted to play sound for removed/inactive player");
            return;
        }

        SoundTask task = new SoundTask(player, sound, category, pos, volume, pitch);
        soundQueue.add(task);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // 处理声音任务
        SoundTask task;
        while ((task = soundQueue.poll()) != null) {
            // 使用final变量解决lambda作用域问题
            final SoundTask finalTask = task;
            soundExecutor.execute(() -> {
                try {
                    ServerPlayer player = finalTask.player();
                    if (player.isRemoved() || !player.isAlive()) {
                        LOGGER.debug("Player not available for sound playback");
                        return;
                    }

                    double distanceSqr = player.distanceToSqr(finalTask.pos().x, finalTask.pos().y, finalTask.pos().z);
                    if (distanceSqr <= (finalTask.volume() * finalTask.volume()) * 256.0) {
                        finalTask.withinRange().set(true);
                    }
                } catch (Exception e) {
                    LOGGER.error("Sound range check failed", e);
                }
            });
        }

    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (SoundTask task : new CopyOnWriteArrayList<>(soundQueue)) {
                if (task.player() == player && task.withinRange().get()) {
                    player.connection.send(new ClientboundSoundPacket(
                            Holder.direct(task.sound()),
                            task.category(),
                            task.pos().x, task.pos().y, task.pos().z,
                            task.volume(), task.pitch(),
                            player.level().getRandom().nextLong()
                    ));
                    soundQueue.remove(task);
                    LOGGER.debug("Sound played for {}: {}",
                            player.getName().getString(),
                            BuiltInRegistries.SOUND_EVENT.getKey(task.sound()));
                }
            }
        }
    }

    private static class SoundTask {
        private final ServerPlayer player;
        private final SoundEvent sound;
        private final SoundSource category;
        private final Vec3 pos;
        private final float volume;
        private final float pitch;
        private final AtomicBoolean withinRange = new AtomicBoolean(false);

        public SoundTask(ServerPlayer player, SoundEvent sound, SoundSource category,
                         Vec3 pos, float volume, float pitch) {
            this.player = player;
            this.sound = sound;
            this.category = category;
            this.pos = pos;
            this.volume = volume;
            this.pitch = pitch;
        }

        public ServerPlayer player() {
            return player;
        }

        public SoundEvent sound() {
            return sound;
        }

        public SoundSource category() {
            return category;
        }

        public Vec3 pos() {
            return pos;
        }

        public float volume() {
            return volume;
        }

        public float pitch() {
            return pitch;
        }

        public AtomicBoolean withinRange() {
            return withinRange;
        }
    }
}