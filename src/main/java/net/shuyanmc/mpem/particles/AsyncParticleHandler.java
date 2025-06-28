package net.shuyanmc.mpem.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.config.CoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class AsyncParticleHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static ExecutorService executorService;
    private static BlockingQueue<ParticleTask> particleQueue = new LinkedBlockingQueue<>();

    public static void init() {

        if (CoolConfig.ASYNC_PARTICLES.get()) {
            int threads = CoolConfig.ASYNC_PARTICLES_THREADS.get();
            executorService = Executors.newFixedThreadPool(threads, r -> {
                Thread t = new Thread(r, "Async Particle Worker");
                t.setDaemon(true);
                return t;
            });

            LOGGER.info("Async Particle System initialized with {} threads", threads);
        }
    }

    public static void shutdown() {

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    public static void addParticle(Level level, ParticleOptions particle, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed) {

        if (!CoolConfig.ASYNC_PARTICLES.get() || level.isClientSide) {
            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
            return;
        }

        particleQueue.offer(new ParticleTask(level, particle, x, y, z, xSpeed, ySpeed, zSpeed));


    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        processParticles();
    }

    private static void processParticles() {
        int processed = 0;

        while (!particleQueue.isEmpty() && processed < 1000) { // Limit particles per tick
            ParticleTask task = particleQueue.poll();
            if (task != null) {
                executorService.execute(() -> {
                    ServerLevel serverLevel = (ServerLevel) task.level;
                    if (serverLevel != null && serverLevel.isLoaded(task.pos())) {
                        serverLevel.sendParticles(task.particle(), task.x(), task.y(), task.z(),
                                1, task.xSpeed(), task.ySpeed(), task.zSpeed(), 1.0);
                    }
                });
                processed++;
            }
        }
    }

    private record ParticleTask(
            Level level,
            ParticleOptions particle,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed
    ) {
        public BlockPos pos() {
            return new BlockPos((int) x, (int) y, (int) z);
        }
    }
}