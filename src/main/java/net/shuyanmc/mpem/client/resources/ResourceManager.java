package net.shuyanmc.mpem.client.resources;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.LinkedHashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ResourceManager implements ResourceManagerReloadListener {
    private static final int MAX_CACHED_RESOURCES = 1000;
    private static final Map<String, Lazy<byte[]>> resourceCache = new LinkedHashMap<>(MAX_CACHED_RESOURCES, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Lazy<byte[]>> eldest) {
            return size() > MAX_CACHED_RESOURCES;
        }
    };

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ResourceManager());
    }

    public static byte[] getCompressedResource(String path) {
        return resourceCache.computeIfAbsent(path, k -> Lazy.<byte[]>of(() -> {
            try (var input = ResourceManager.class.getResourceAsStream(path)) {
                return input.readAllBytes();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load resource: " + path, e);
            }
        })).get();
    }

    @Override
    public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager) {
// 由于 MpemMod 类中未定义 getExecutorService() 方法，直接在当前线程中清除缓存
        resourceCache.clear();
    }
}