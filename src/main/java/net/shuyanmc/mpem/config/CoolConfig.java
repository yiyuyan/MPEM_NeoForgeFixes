package net.shuyanmc.mpem.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class CoolConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // ==================== 实体优化 | Entity Optimization ====================
    public static ModConfigSpec.BooleanValue disableEntityCollisions;
    public static ModConfigSpec.BooleanValue optimizeEntityAI;
    public static ModConfigSpec.IntValue entityActivationRange;
    public static final ModConfigSpec.BooleanValue OPTIMIZE_ENTITY_CLEANUP;
    public static final ModConfigSpec.BooleanValue reduceEntityUpdates;

    // 新增实体优化配置
    public static ModConfigSpec.BooleanValue optimizeEntities;
    public static ModConfigSpec.BooleanValue ignoreDeadEntities;
    public static ModConfigSpec.BooleanValue optimizeItems;
    public static ModConfigSpec.ConfigValue<List<? extends String>> itemWhitelist;
    public static ModConfigSpec.IntValue horizontalRange;
    public static ModConfigSpec.IntValue verticalRange;
    public static ModConfigSpec.BooleanValue tickRaidersInRaid;

    // ==================== 物品优化 | Item Optimization ====================
    public static ModConfigSpec.IntValue maxStackSize;
    public static ModConfigSpec.DoubleValue mergeDistance;
    public static ModConfigSpec.IntValue listMode;
    public static ModConfigSpec.ConfigValue<List<? extends String>> itemList;
    public static ModConfigSpec.BooleanValue showStackCount;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue MAX_STACK_SIZE;

    // ==================== 内存优化 | Memory Optimization ====================
    public static final ModConfigSpec.IntValue MEMORY_CLEAN_INTERVAL;
    public static final ModConfigSpec.BooleanValue ENABLE_GC;

    // ==================== 调试选项 | Debug Options ====================
    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;
    public static final ModConfigSpec.BooleanValue LOG_BLOCK_EVENTS;

    // ==================== 区块优化 | Chunk Optimization ====================
    public static ModConfigSpec.BooleanValue aggressiveChunkUnloading;
    public static ModConfigSpec.IntValue chunkUnloadDelay;
    public static final ModConfigSpec.BooleanValue reduceChunkUpdates;
    public static final ModConfigSpec.BooleanValue filterRedundantBlockUpdates;
    public static final ModConfigSpec.IntValue CHUNK_GEN_THREADS;

    // ==================== 异步优化 | Async Optimization ====================
    public static final ModConfigSpec.BooleanValue ASYNC_PARTICLES;
    public static final ModConfigSpec.IntValue ASYNC_PARTICLES_THREADS;
    public static final ModConfigSpec.IntValue AI_THREADS;
    public static final ModConfigSpec.IntValue MAX_ASYNC_OPERATIONS_PER_TICK;
    public static final ModConfigSpec.BooleanValue DISABLE_ASYNC_ON_ERROR;
    public static final ModConfigSpec.IntValue ASYNC_EVENT_TIMEOUT;
    public static final ModConfigSpec.BooleanValue WAIT_FOR_ASYNC_EVENTS;
    public static ModConfigSpec.IntValue maxCPUPro;
    public static ModConfigSpec.IntValue maxthreads;

    // ==================== 事件系统 | Event System ====================
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_OPTIMIZATION;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ASYNC_EVENT_CLASS_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ASYNC_EVENT_MOD_BLACKLIST;
    public static final ModConfigSpec.BooleanValue STRICT_CLASS_CHECKING;

    // ==================== 反作弊系统 | Anti-Cheat System ====================
    public static final ModConfigSpec.BooleanValue ANTI_CHEAT_ENABLED;
    public static final ModConfigSpec.IntValue DETECTION_DELAY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CLIENT_CLASS_WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_WHITELIST;
    public static final ModConfigSpec.BooleanValue CLASS_NAME_DETECTION_ENABLED;

    // ==================== 帧率控制 | Frame Rate Control ====================
    public static final ModConfigSpec.BooleanValue REDUCE_FPS_WHEN_INACTIVE;
    public static final ModConfigSpec.IntValue INACTIVE_FPS_LIMIT;

    // ==================== 渲染距离 | Render Distance ====================
    public static final ModConfigSpec.BooleanValue REDUCE_RENDER_DISTANCE_WHEN_INACTIVE;
    public static final ModConfigSpec.IntValue INACTIVE_RENDER_DISTANCE;



    static {
        // ==================== 实体优化设置 | Entity Optimization Settings ====================
        BUILDER.comment("实体优化设置 | Entity Optimization Settings").push("entity_optimization");

        disableEntityCollisions = BUILDER
                .comment("优化实体碰撞检测 | Optimize entity collision detection")
                .define("disableEntityCollisions", true);

        optimizeEntityAI = BUILDER
                .comment("优化实体AI计算 | Optimize entity AI calculations")
                .define("optimizeEntityAI", true);

        entityActivationRange = BUILDER
                .comment("实体激活范围 (方块) | Entity activation range (blocks)")
                .defineInRange("entityActivationRange", 32, 16, 128);

        OPTIMIZE_ENTITY_CLEANUP = BUILDER
                .comment("启用死亡实体清理 | Enable dead entity cleanup")
                .define("entityCleanup", true);

        reduceEntityUpdates = BUILDER
                .comment("减少远处实体的同步频率 | Reduce entity sync frequency for distant entities")
                .define("reduceEntityUpdates", true);

        // 新增实体优化配置
        optimizeEntities = BUILDER
                .comment("是否优化实体tick | Optimize entity ticking")
                .define("optimizeEntities", true);

        ignoreDeadEntities = BUILDER
                .comment("是否忽略已死亡的实体 | Ignore dead entities")
                .define("ignoreDeadEntities", false);

        optimizeItems = BUILDER
                .comment("是否优化物品实体 | Optimize item entities")
                .define("optimizeItems", true);

        itemWhitelist = BUILDER
                .comment("物品白名单（不会被优化的物品）| Item whitelist (items that will not be optimized)")
                .defineList("itemWhitelist", Collections.singletonList("minecraft:nether_star"),
                        o -> o instanceof String);

        horizontalRange = BUILDER
                .comment("实体激活范围（水平方向）| Horizontal activation range for entities (blocks)")
                .defineInRange("horizontalRange", 32, 8, 128);

        verticalRange = BUILDER
                .comment("实体激活范围（垂直方向）| Vertical activation range for entities (blocks)")
                .defineInRange("verticalRange", 16, 8, 64);

        tickRaidersInRaid = BUILDER
                .comment("在袭击中是否强制tick袭击者 | Force tick raiders during raids")
                .define("tickRaidersInRaid", true);

        BUILDER.pop();

        // ==================== 帧率控制设置 | Frame Rate Control Settings ====================
        BUILDER.comment("帧率控制设置 | Frame Rate Control Settings").push("frame_rate");

        REDUCE_FPS_WHEN_INACTIVE = BUILDER
                .comment("是否在窗口非活动时降低FPS | Reduce FPS when window is inactive")
                .define("reduceFpsWhenInactive", true);

        INACTIVE_FPS_LIMIT = BUILDER
                .comment("非活动状态时的FPS限制 | FPS limit when inactive")
                .defineInRange("inactiveFpsLimit", 30, 5, 120);

        BUILDER.pop();

        // ==================== 渲染距离设置 | Render Distance Settings ====================
        BUILDER.comment("渲染距离设置 | Render Distance Settings").push("render_distance");

        REDUCE_RENDER_DISTANCE_WHEN_INACTIVE = BUILDER
                .comment("是否在窗口非活动时降低渲染距离 | Reduce render distance when window is inactive")
                .define("reduceRenderDistanceWhenInactive", true);

        INACTIVE_RENDER_DISTANCE = BUILDER
                .comment("非活动状态时的渲染距离 | Render distance when inactive")
                .defineInRange("inactiveRenderDistance", 4, 2, 32);

        BUILDER.pop();



        // ==================== 物品优化设置 | Item Optimization Settings ====================
        BUILDER.comment("物品优化设置 | Item Optimization Settings").push("item_optimization");

        maxStackSize = BUILDER
                .comment("合并物品的最大堆叠数量（0表示无限制）| Maximum stack size for merged items (0 = no limit)")
                .defineInRange("maxStackSize", 0, 0, Integer.MAX_VALUE);

        mergeDistance = BUILDER
                .comment("物品合并检测半径（单位：方块）| Item merge detection radius in blocks")
                .defineInRange("mergeDistance", 0.5, 0.1, 10.0);

        listMode = BUILDER
                .comment("0: 禁用 1: 白名单模式 2: 黑名单模式 | 0: Disabled, 1: Whitelist, 2: Blacklist")
                .defineInRange("listMode", 0, 0, 2);

        itemList = BUILDER
                .comment("白名单/黑名单中的物品注册名列表 | Item registry names for whitelist/blacklist")
                .defineList("itemList", Collections.emptyList(), o -> o instanceof String);

        showStackCount = BUILDER
                .comment("是否在合并后的物品上显示堆叠数量 | Whether to show stack count on merged items")
                .define("showStackCount", true);

        BUILDER.push("stack_size");

        ENABLED = BUILDER
                .comment("启用自定义堆叠大小 | Enable custom stack sizes")
                .define("enabled", true);

        MAX_STACK_SIZE = BUILDER
                .comment("最大物品堆叠大小 (1-9999) | Maximum item stack size (1-9999)")
                .defineInRange("maxStackSize", 64, 1, 9999);

        BUILDER.pop();
        BUILDER.pop();

        // ==================== 内存优化设置 | Memory Optimization Settings ====================
        BUILDER.comment("内存优化设置 | Memory Optimization Settings").push("memory_optimization");

        MEMORY_CLEAN_INTERVAL = BUILDER
                .comment("内存清理间隔(秒) | Memory cleanup interval (seconds)")
                .defineInRange("cleanInterval", 300, 60, 3600);

        ENABLE_GC = BUILDER
                .comment("是否在清理时触发垃圾回收 | Whether to trigger garbage collection during cleanup")
                .define("enableGC", true);

        BUILDER.pop();

        // ==================== 调试设置 | Debug Settings ====================
        BUILDER.comment("调试设置 | Debug Settings").push("debug");

        DEBUG_LOGGING = BUILDER
                .comment("启用调试日志 | Enable debug logging")
                .define("debug", false);

        LOG_BLOCK_EVENTS = BUILDER
                .comment("记录方块相关事件 | Log block related events")
                .define("logBlockEvents", false);

        BUILDER.pop();

        // ==================== 区块优化设置 | Chunk Optimization Settings ====================
        BUILDER.comment("区块优化设置 | Chunk Optimization Settings").push("chunk_optimization");

        aggressiveChunkUnloading = BUILDER
                .comment("主动卸载非活动区块 | Aggressively unload inactive chunks")
                .define("aggressiveChunkUnloading", true);

        chunkUnloadDelay = BUILDER
                .comment("区块卸载延迟 (秒) | Chunk unload delay (seconds)")
                .defineInRange("chunkUnloadDelay", 60, 10, 600);

        reduceChunkUpdates = BUILDER
                .comment("当玩家移动短距离时减少区块更新频率 | Reduce chunk update frequency when player moves short distances")
                .define("reduceChunkUpdates", true);

        filterRedundantBlockUpdates = BUILDER
                .comment("过滤冗余的方块更新数据包 | Filter redundant block update packets")
                .define("filterRedundantBlockUpdates", true);

        CHUNK_GEN_THREADS = BUILDER
                .comment("异步区块生成的线程数 | Number of threads for async chunk generation")
                .defineInRange("chunkGenThreads", 2, 1, 8);

        BUILDER.pop();

        // ==================== 异步优化设置 | Async Optimization Settings ====================
        BUILDER.comment("异步优化设置 | Async Optimization Settings").push("async_optimization");

        ASYNC_PARTICLES = BUILDER
                .comment("启用异步粒子处理 | Enable asynchronous particle processing")
                .define("asyncParticles", true);

        ASYNC_PARTICLES_THREADS = BUILDER
                .comment("用于异步粒子的线程数 (推荐1-4) | Number of threads to use for async particles (1-4 recommended)")
                .defineInRange("asyncParticlesThreads", 2, 1, Runtime.getRuntime().availableProcessors());

        AI_THREADS = BUILDER
                .comment("AI处理的线程数 | Number of threads for AI processing")
                .defineInRange("aiThreads", 2, 1, 8);

        MAX_ASYNC_OPERATIONS_PER_TICK = BUILDER
                .comment("每tick处理的最大异步操作数 | Max async operations processed per tick")
                .defineInRange("maxAsyncOpsPerTick", 1000, 100, 10000);

        DISABLE_ASYNC_ON_ERROR = BUILDER
                .comment("出错后禁用该事件类型的异步处理 | Disable async for event type after errors")
                .define("disableAsyncOnError", true);

        ASYNC_EVENT_TIMEOUT = BUILDER
                .comment("异步事件超时时间(秒) | Timeout in seconds for async events")
                .defineInRange("asyncEventTimeout", 2, 1, 10);

        WAIT_FOR_ASYNC_EVENTS = BUILDER
                .comment("等待异步事件完成 | Wait for async events to complete")
                .define("waitForAsyncEvents", false);

        BUILDER.push("async_cpu_config");

        maxCPUPro = BUILDER
                .comment("异步系统最大CPU核心数 | Max CPU Cores for async system (only for async threads, not world async)")
                .defineInRange("maxCPUPro", 2, 2, 9999);

        maxthreads = BUILDER
                .comment("最大线程数 | Max Threads (only for general async threads, not dedicated async threads)")
                .defineInRange("maxthreads", 2, 2, 9999);

        BUILDER.pop();
        BUILDER.pop();

        // ==================== 事件系统设置 | Event System Settings ====================
        BUILDER.comment("事件系统设置 | Event System Settings").push("event_system");

        ENABLE_EVENT_OPTIMIZATION = BUILDER
                .comment("启用高性能事件系统优化 | Enable High-Performance event system optimization")
                .define("enableOptimization", true);

        ASYNC_EVENT_CLASS_BLACKLIST = BUILDER
                .comment("不应异步处理的事件类列表 | List of event classes that should NOT be processed asynchronously",
                        "支持通配符 (如 'net.minecraftforge.event.entity.living.*') | Supports wildcards (e.g. 'net.minecraftforge.event.entity.living.*')")
                .defineList("classBlacklist",
                        List.of(
                                "net.minecraftforge.event.TickEvent",
                                "net.minecraftforge.event.level.LevelTickEvent",
                                "net.minecraftforge.event.entity.living.*"
                        ),
                        o -> o instanceof String);

        ASYNC_EVENT_MOD_BLACKLIST = BUILDER
                .comment("不应异步处理的模组ID列表 | List of mod IDs whose events should NOT be processed asynchronously")
                .defineList("modBlacklist", Collections.emptyList(), o -> o instanceof String);

        STRICT_CLASS_CHECKING = BUILDER
                .comment("启用严格的类存在检查 (推荐为稳定性) | Enable strict class existence checking (recommended for stability)")
                .define("strictClassChecking", true);

        BUILDER.pop();

        // ==================== 反作弊系统设置 | Anti-Cheat System Settings ====================
        BUILDER.comment("反作弊系统设置 (仅服务器生效) | Anti-Cheat System Settings (Server Only)").push("anti_cheat");

        ANTI_CHEAT_ENABLED = BUILDER
                .comment("是否启用反作弊系统 | Whether to enable anti-cheat system")
                .define("enabled", true);

        DETECTION_DELAY = BUILDER
                .comment("检测延迟时间（秒） | Detection delay (seconds)",
                        "建议值3-5秒，避免登录时冲突 | Recommended 3-5 seconds to avoid login conflicts")
                .defineInRange("detectionDelay", 3, 1, 10);

        CLASS_NAME_DETECTION_ENABLED = BUILDER
                .comment("是否启用类名安全检测 | Whether to enable class name security detection")
                .define("classNameDetectionEnabled", false);

        CLIENT_CLASS_WHITELIST = BUILDER
                .comment("客户端类名白名单 (支持通配符) | Client class name whitelist (supports wildcards)")
                .defineList("clientClassWhitelist",
                        List.of(
                                "com.example.safemod.*",
                                "net.shuyanmc.mpem.*"
                        ),
                        o -> o instanceof String);

        MOD_WHITELIST = BUILDER
                .comment("豁免模组ID列表 | Exempt mod ID list")
                .defineList("modWhitelist", Collections.emptyList(), o -> o instanceof String);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    // ==================== 工具方法 | Utility Methods ====================
    public static Set<String> getAsyncEventClassBlacklist() {
        return new HashSet<>(ASYNC_EVENT_CLASS_BLACKLIST.get());
    }

    public static boolean isDetectionEnabled() {
        return CLASS_NAME_DETECTION_ENABLED.get();
    }

    public static Set<String> getAsyncEventModBlacklist() {
        return new HashSet<>(ASYNC_EVENT_MOD_BLACKLIST.get());
    }

    public static boolean isStrictClassCheckingEnabled() {
        return STRICT_CLASS_CHECKING.get();
    }

    public static Set<String> getClientClassWhitelist() {
        return new HashSet<>(CLIENT_CLASS_WHITELIST.get());
    }

    public static Set<String> getModWhitelist() {
        return new HashSet<>(MOD_WHITELIST.get());
    }
}