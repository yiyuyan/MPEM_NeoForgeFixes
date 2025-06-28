package net.shuyanmc.mpem.async;


import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.async.entity.AsyncAIManager;
import net.shuyanmc.mpem.async.entity.AsyncCollisionSystem;
import net.shuyanmc.mpem.async.player.AsyncPlayerData;
import net.shuyanmc.mpem.async.redstone.AsyncRedstone;
import net.shuyanmc.mpem.async.resources.AsyncResourceLoader;
import net.shuyanmc.mpem.async.sound.AsyncSoundSystem;
import net.shuyanmc.mpem.async.world.StructureGenAsync;
@AsyncHandler
public class AsyncSystemInitializer {

    public static void init(FMLCommonSetupEvent event) {
        // 初始化所有系统
        StructureGenAsync.init();
        AsyncAIManager.init();
        AsyncCollisionSystem.init();
        AsyncPlayerData.init();
        AsyncResourceLoader.init();
        AsyncRedstone.init();
        AsyncSoundSystem.init();
        
        // 注册事件处理
        NeoForge.EVENT_BUS.register(StructureGenAsync.class);
        NeoForge.EVENT_BUS.register(AsyncAIManager.class);
        NeoForge.EVENT_BUS.register(AsyncCollisionSystem.class);
        NeoForge.EVENT_BUS.register(AsyncPlayerData.class);
        NeoForge.EVENT_BUS.register(AsyncResourceLoader.class);
        NeoForge.EVENT_BUS.register(AsyncRedstone.class);
        NeoForge.EVENT_BUS.register(AsyncSoundSystem.class);
        
        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(AsyncSystemInitializer::shutdown));
    }
    
    public static void shutdown() {
        // 关闭所有线程池

        StructureGenAsync.shutdown();
        AsyncAIManager.shutdown();
        AsyncCollisionSystem.shutdown();
        AsyncPlayerData.shutdown();
        AsyncResourceLoader.shutdown();
        AsyncRedstone.shutdown();

        AsyncSoundSystem.shutdown();
    }
}