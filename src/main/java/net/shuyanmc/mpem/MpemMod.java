package net.shuyanmc.mpem;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.shuyanmc.mpem.async.AsyncSystemInitializer;
import net.shuyanmc.mpem.client.ItemCountRenderer;
import net.shuyanmc.mpem.config.CoolConfig;
import net.shuyanmc.mpem.events.ModEventHandlers;
import net.shuyanmc.mpem.particles.AsyncParticleHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Mod(value = "mpem")
public class MpemMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mpem";
    public static final String VERSION = "2.0.5";
    private static final String PROTOCOL_VERSION = "1";
    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();
    @AsyncHandler
    private static boolean removeByteBufTagLimit = true;
    private static BlockPos protectedAreaStart = new BlockPos(0, 0, 0);
    private static BlockPos protectedAreaEnd = new BlockPos(0, 0, 0);
    private static boolean optimizationEnabled = false;
    /*
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, MODID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );*/
    private static int messageID = 0;

    public MpemMod(ModContainer container) {
        IEventBus modEventBus = container.getEventBus();
        IEventBus forgeEventBus = NeoForge.EVENT_BUS;
        container.registerConfig(ModConfig.Type.COMMON, CoolConfig.SPEC, "mpem.toml");
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onRegisterPayloadHandlers);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
            NeoForge.EVENT_BUS.register(ItemCountRenderer.class);
        }
        MixinBootstrap.init();
        ModEventHandlers.register(modEventBus, forgeEventBus);
        modEventBus.addListener(AsyncSystemInitializer::init);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AsyncEventSystem.shutdown();
            AsyncParticleHandler.shutdown();
        }));

        LOGGER.info("Initializing MPEM MOD v{}", VERSION);
        LOGGER.info("MPEM模组启动中...");
        System.gc();
        LOGGER.info("先来一次GC清理！");
        LOGGER.info("再初始化以下模组...");
        LOGGER.info("请确保模组名称前面有英文半角符号的'!',这样模组才会第一个加载！会有更好的优化！");
        LOGGER.info("通知：反作弊默认关闭类名检测，有需要的服务器管理员可以打开，在2.0.6更新后，本模组基本不与常见模组发送误报，可放心使用，如果仍然有一些模组误报的，可以在SYMC玩家交流群反馈！");
        LOGGER.info("SYMC玩家QQ交流群：372378451");
        LOGGER.info("暑假来临，SYMC团队在这里祝大家暑假快乐\n" +
                "暑假期间，请大家注意安全，（Zcraft今天出门就踩到一颗钉子（谁这么没有公德心/(╬▔皿▔)）\n" +
                "注意防水防火防电（这都是老话了...）");
		/*
		ModLoadingContext.get().registerExtensionPoint(
				IExtensionPoint.DisplayTest.class,
				() -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true)
		);*/

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private static void cleanMostFrequentMonsters(MinecraftServer server) {
        ServerLevel level = server.overworld();
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof Monster && isInProtectedArea(entity.blockPosition())) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private static boolean isInProtectedArea(BlockPos pos) {
        return pos.getX() >= Math.min(protectedAreaStart.getX(), protectedAreaEnd.getX()) &&
                pos.getX() <= Math.max(protectedAreaStart.getX(), protectedAreaEnd.getX()) &&
                pos.getY() >= Math.min(protectedAreaStart.getY(), protectedAreaEnd.getY()) &&
                pos.getY() <= Math.max(protectedAreaStart.getY(), protectedAreaEnd.getY()) &&
                pos.getZ() >= Math.min(protectedAreaStart.getZ(), protectedAreaEnd.getZ()) &&
                pos.getZ() <= Math.max(protectedAreaStart.getZ(), protectedAreaEnd.getZ());
    }

    public static void registerDynamicListener(
            Class<? extends Event> eventType,
            EventListener listener,
            EventPriority priority,
            boolean receiveCancelled
    ) {
        NeoForge.EVENT_BUS.addListener(
                priority,
                receiveCancelled,
                eventType,
                event -> {
                    try {
                        listener.invoke(event);
                    } catch (ClassCastException e) {
                        LOGGER.error("Event type mismatch for listener", e);
                    } catch (Throwable t) {
                        LOGGER.error("Error in optimized event handler", t);
                        if (CoolConfig.DISABLE_ASYNC_ON_ERROR.get() && eventType.getSimpleName().contains("Async")) {
                            LOGGER.warn("Disabling async for event type due to handler error: {}", eventType.getName());
                            AsyncEventSystem.registerSyncEvent(eventType);
                        }
                    }
                }
        );
    }

    public static void executeSafeAsync(Runnable task, String taskName) {
        AsyncEventSystem.executeAsync(
                ServerTickEvent.class,
                () -> {
                    try {
                        long start = System.currentTimeMillis();
                        task.run();
                        long duration = System.currentTimeMillis() - start;
                        if (duration > 100) {
                            LOGGER.debug("Async task '{}' completed in {}ms", taskName, duration);
                        }
                    } catch (Throwable t) {
                        LOGGER.error("Async task '{}' failed", taskName, t);
                        throw t;
                    }
                }
        );
    }

    private void onRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event) {
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("MPEM Mod 初始化完成");
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        event.enqueueWork(() -> {
            //NeoForge.EVENT_BUS.register(EventHandlers.class);
            NeoForge.EVENT_BUS.register(EventHandlersTRO.class);
            EventHandlersTRO.registerOptimizedHandlers();
            AsyncEventSystem.initialize();
            ModEventProcessor.processModEvents();
            warmUpClasses();
        });
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.debug("通用设置初始化");
    }

	/*??
	public static <T> void addNetworkMessage(Class<T> messageType,
											 BiConsumer<T, FriendlyByteBuf> encoder,
											 Function<FriendlyByteBuf, T> decoder,
											 BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID++, messageType, encoder, decoder, messageConsumer);
	}*/

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("客户端设置初始化");
        //NeoForge.EVENT_BUS.register(new ClientEventListener());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (Minecraft.getInstance().isSingleplayer()) {
            return;
        }

        LOGGER.debug("玩家登录事件触发，准备启动反作弊检测");
        AsyncEventSystem.executeAsync(
                ClientPlayerNetworkEvent.LoggingIn.class,
                () -> {
                    try {
                        TimeUnit.SECONDS.sleep(CoolConfig.DETECTION_DELAY.get());
                        if (Minecraft.getInstance().getConnection() != null) {
                            LOGGER.info("开始执行客户端反作弊扫描...");
                            CheatDetector.runClientDetection();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.warn("检测线程被中断");
                    } catch (Exception e) {
                        LOGGER.error("反作弊检测失败", e);
                    }
                }
        );
    }

    private void warmUpClasses() {
        long start = System.currentTimeMillis();
        String[] classesToWarm = {
                "net.minecraft.world.level.block.Block",
                "net.minecraft.world.item.Item",
                "net.minecraft.world.entity.EntityType",
                "net.minecraft.core.Registry"
        };

        for (String className : classesToWarm) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                LOGGER.debug("Warmup class not found: {}", className);
            }
        }
        LOGGER.debug("Class warmup completed in {}ms", System.currentTimeMillis() - start);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("spawnT2")
                .then(Commands.argument("pos1", Vec3Argument.vec3())
                        .then(Commands.argument("pos2", Vec3Argument.vec3())
                                .executes(context -> {
                                    Vec3 pos1 = Vec3Argument.getVec3(context, "pos1");
                                    Vec3 pos2 = Vec3Argument.getVec3(context, "pos2");
                                    protectedAreaStart = new BlockPos((int) pos1.x, (int) pos1.y, (int) pos1.z);
                                    protectedAreaEnd = new BlockPos((int) pos2.x, (int) pos2.y, (int) pos2.z);
                                    return 1;
                                })
                        )
                ));
    }
}