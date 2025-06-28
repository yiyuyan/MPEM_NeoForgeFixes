package net.shuyanmc.mpem.async.resources;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.shuyanmc.mpem.AsyncHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@AsyncHandler
public class AsyncResourceLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<ResourceTask> resourceQueue = new LinkedBlockingQueue<>();
    private static final ConcurrentLinkedQueue<ResourceTask> completedTasks = new ConcurrentLinkedQueue<>();
    private static ExecutorService resourceExecutor;

    public static void init() {
        int cores = Runtime.getRuntime().availableProcessors();
        resourceExecutor = Executors.newWorkStealingPool(cores);
        LOGGER.info("Async Resource Loader initialized with work-stealing pool ({} threads)", cores);
    }

    public static void shutdown() {
        if (resourceExecutor != null) {
            resourceExecutor.shutdownNow();
            try {
                if (!resourceExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.warn("Resource loader thread pool did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void loadResourcePackAsync(PackRepository repository, Path packPath) {
        ResourceTask task = new ResourceTask(repository, packPath);
        resourceQueue.add(task);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        // 处理已完成的任务（在主线程）
        ResourceTask completedTask;
        while ((completedTask = completedTasks.poll()) != null) {
            if (completedTask.error().get() == null) {
                PackResources resources = completedTask.resources().get();
                if (resources != null) {
                    try {
                        String packName = completedTask.packPath().getFileName().toString();
                        PackLocationInfo locationInfo = new PackLocationInfo(resources.packId(), Component.literal(packName), PackSource.BUILT_IN, Optional.empty());
                        PackSelectionConfig selectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);
                        Pack.ResourcesSupplier resourcesSupplier = new PathPackResources.PathResourcesSupplier(completedTask.packPath);
                        Pack pack = Pack.readMetaAndCreate(locationInfo, resourcesSupplier, PackType.SERVER_DATA, selectionConfig);

                        if (pack != null) {
                            // 使用新的方法添加资源包
                            // 由于 getSelectedPacks() 返回的可能是 Collection<Pack>，需要转换为 List<Pack>
                            Collection<Pack> packs = completedTask.repository().getSelectedPacks();
                            List<Pack> selected = new ArrayList<>(packs);
                            selected.add(pack);
                            // 由于 setSelected 方法需要的是 Collection<String>，我们需要从 Pack 对象中提取名称
                            List<String> packNames = new ArrayList<>();
                            // 为避免变量名冲突，将变量名从 pack 改为 newPack
                            for (Pack newPack : selected) {
                                packNames.add(pack.getId());
                            }
                            completedTask.repository().setSelected(packNames);
                            completedTask.repository().reload();
                            LOGGER.info("Resource pack loaded: {}", packName);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to register resource pack", e);
                    }
                }
            }
        }

        // 提交新任务到线程池
        ResourceTask task;
        while ((task = resourceQueue.poll()) != null) {
            final ResourceTask finalTask = task;
            resourceExecutor.execute(() -> {
                try {
                    PackResources resources = loadPackResources(finalTask.packPath());
                    if (resources != null) {
                        finalTask.resources().set(resources);
                    } else {
                        throw new IllegalStateException("Failed to load pack: " + finalTask.packPath());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load resource pack", e);
                    finalTask.error().set(e);
                } finally {
                    completedTasks.add(finalTask);
                }
            });
        }

    }

    private static PackResources loadPackResources(Path packPath) throws IOException {
        if (!Files.exists(packPath)) {
            throw new IOException("Resource pack not found: " + packPath);
        }
        String packName = packPath.getFileName().toString();
        PackLocationInfo locationInfo = new PackLocationInfo(packName, Component.literal(packName), PackSource.BUILT_IN, Optional.empty());
        return new PathPackResources(
                locationInfo,
                packPath
        );
    }

    private static class ResourceTask {
        private final PackRepository repository;
        private final Path packPath;
        private final AtomicReference<PackResources> resources = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();

        public ResourceTask(PackRepository repository, Path packPath) {
            this.repository = repository;
            this.packPath = packPath;
        }

        public PackRepository repository() {
            return repository;
        }

        public Path packPath() {
            return packPath;
        }

        public AtomicReference<PackResources> resources() {
            return resources;
        }

        public AtomicReference<Exception> error() {
            return error;
        }
    }
}