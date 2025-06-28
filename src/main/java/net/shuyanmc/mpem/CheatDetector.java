package net.shuyanmc.mpem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.shuyanmc.mpem.network.NetworkHandler;

import java.util.*;
import java.util.regex.Pattern;

public class CheatDetector {
    public static final List<String> EXEMPT_SIGNATURES = Arrays.asList(
            "net.shuyanmc.*",
            "net.shuyanmc.",
            "net.shuyanmc",
            "net.minecraftforge.",
            "net.fabricmc.",
            "com.sinytra.",
            "com.sinytra.",
            "org.sinytra.",
            "com.jamieswhiteshirt.",
            "reloc.com.jamieswhiteshirt.",
            "com.jamieswhiteshirt.*",
            "com.jamieswhiteshirt.reachentityattributes.*",
            "com.jamieswhiteshirt.reachentityattributes",
            "com.jamieswhiteshirt.reachentityattributes.",
            "reloc.com.jamieswhiteshirt.reachentityattributes.",
            "reloc.com.jamieswhiteshirt.*",
            "reloc.com.jamieswhiteshirt.reachentityattributes.*",
            "reloc.com.jamieswhiteshirt.reachentityattributes",
            "reloc.net.fabricmc",
            "reloc.net.fabricmc.*",
            "net.fabricmc.*",
            "reloc.net.minecraftforge",
            "reloc.net.minecraftforge.*",
            "sat4j.",
            "reloc",
            "reloc.*",
            "reloc.org.sat4j",
            "reloc.org.sat4j.*",
            "io.github.legacyfabric.",
            "io.github.legacyfabric",
            "io.github.legacyfabric.*",
            "me.zeroeightsix.fabric.",
            "me.zeroeightsix.fabric",
            "appeng.client.guidebook", "appeng.client.guidebook.", "appeng.client.guidebook.*",
            "appeng.client.render", "appeng.client.render.", "appeng.client.render.*",
            "appeng.debug", "appeng.debug.", "appeng.debug.*",
            "appeng.items.misc", "appeng.items.misc.", "appeng.items.misc.*",
            "appeng.worldgen", "appeng.worldgen.", "appeng.worldgen.*",
            "appeng.worldgen.meteorite", "appeng.worldgen.meteorite.", "appeng.worldgen.meteorite.*",
            "biomesoplenty.worldgen", "biomesoplenty.worldgen.", "biomesoplenty.worldgen.*",
            "blusunrize.immersiveengineering", "blusunrize.immersiveengineering.", "blusunrize.immersiveengineering.*",
            "com.aetherteam.aether", "com.aetherteam.aether.", "com.aetherteam.aether.*",
            "com.bawnorton.neruina", "com.bawnorton.neruina.", "com.bawnorton.neruina.*",
            "com.blamejared.crafttweaker", "com.blamejared.crafttweaker.", "com.blamejared.crafttweaker.*",
            "com.craisinlord.integrated_api", "com.craisinlord.integrated_api.", "com.craisinlord.integrated_api.*",
            "com.dinzeer.legendblade", "com.dinzeer.legendblade.", "com.dinzeer.legendblade.*",
            "com.github.L_Ender.cataclysm", "com.github.L_Ender.cataclysm.", "com.github.L_Ender.cataclysm.*",
            "com.github.alexmodguy.alexscaves", "com.github.alexmodguy.alexscaves.", "com.github.alexmodguy.alexscaves.*",
            "com.github.alexthe666.alexsmobs", "com.github.alexthe666.alexsmobs.", "com.github.alexthe666.alexsmobs.*",
            "com.github.alexthe666.citadel", "com.github.alexthe666.citadel.", "com.github.alexthe666.citadel.*",
            "com.github.tartaricacid.netmusic", "com.github.tartaricacid.netmusic.", "com.github.tartaricacid.netmusic.*",
            "com.github.tartaricacid.touhoulittlemaid", "com.github.tartaricacid.touhoulittlemaid.", "com.github.tartaricacid.touhoulittlemaid.*",
            "com.github.wallev.maidsoulkitchen", "com.github.wallev.maidsoulkitchen.", "com.github.wallev.maidsoulkitchen.*",
            "com.github.wallev.verhelper", "com.github.wallev.verhelper.", "com.github.wallev.verhelper.*",
            "com.happyrespawnanchor.resharpedrenderfixpatch", "com.happyrespawnanchor.resharpedrenderfixpatch.", "com.happyrespawnanchor.resharpedrenderfixpatch.*",
            "com.hlysine.create_connected", "com.hlysine.create_connected.", "com.hlysine.create_connected.*",
            "com.legacy.lucent", "com.legacy.lucent.", "com.legacy.lucent.*",
            "com.legacy.structure_gel", "com.legacy.structure_gel.", "com.legacy.structure_gel.*",
            "com.mrcrayfish.framework", "com.mrcrayfish.framework.", "com.mrcrayfish.framework.*",
            "com.mrcrayfish.furniture", "com.mrcrayfish.furniture.", "com.mrcrayfish.furniture.*",
            "com.natamus.collective", "com.natamus.collective.", "com.natamus.collective.*",
            "com.nick.chimes", "com.nick.chimes.", "com.nick.chimes.*",
            "com.oierbravo.createsifter", "com.oierbravo.createsifter.", "com.oierbravo.createsifter.*",
            "com.probejs.docs", "com.probejs.docs.", "com.probejs.docs.*",
            "com.simibubi.create", "com.simibubi.create.", "com.simibubi.create.*",
            "com.teamabnormals.autumnity", "com.teamabnormals.autumnity.", "com.teamabnormals.autumnity.*",
            "com.teamabnormals.blueprint", "com.teamabnormals.blueprint.", "com.teamabnormals.blueprint.*",
            "com.teamtea.eclipticseasons", "com.teamtea.eclipticseasons.", "com.teamtea.eclipticseasons.*",
            "com.yungnickyoung.minecraft", "com.yungnickyoung.minecraft.", "com.yungnickyoung.minecraft.*",
            "committee.nova.mods", "committee.nova.mods.", "committee.nova.mods.*",
            "cofh.lib.common", "cofh.lib.common.", "cofh.lib.common.*",
            "customskinloader.loader", "customskinloader.loader.", "customskinloader.loader.*",
            "customskinloader.utils", "customskinloader.utils.", "customskinloader.utils.*",
            "de.cheaterpaul.fallingleaves", "de.cheaterpaul.fallingleaves.", "de.cheaterpaul.fallingleaves.*",
            "de.keksuccino.fancymenu", "de.keksuccino.fancymenu.", "de.keksuccino.fancymenu.*",
            "dev.architectury.event", "dev.architectury.event.", "dev.architectury.event.*",
            "dev.architectury.registry", "dev.architectury.registry.", "dev.architectury.registry.*",
            "dev.ftb.mods.ftblibrary", "dev.ftb.mods.ftblibrary.", "dev.ftb.mods.ftblibrary.*",
            "dev.ftb.mods.ftbquests", "dev.ftb.mods.ftbquests.", "dev.ftb.mods.ftbquests.*",
            "dev.ftb.mods.ftbteams", "dev.ftb.mods.ftbteams.", "dev.ftb.mods.ftbteams.*",
            "dev.ghen.thirst", "dev.ghen.thirst.", "dev.ghen.thirst.*",
            "dev.latvian.mods", "dev.latvian.mods.", "dev.latvian.mods.*",
            "dev.toma.configuration", "dev.toma.configuration.", "dev.toma.configuration.*",
            "dev.xkmc.youkaishomecoming", "dev.xkmc.youkaishomecoming.", "dev.xkmc.youkaishomecoming.*",
            "earth.terrarium.adastra", "earth.terrarium.adastra.", "earth.terrarium.adastra.*",
            "fabric.fun.qu_an", "fabric.fun.qu_an.", "fabric.fun.qu_an.*",
            "forge.fun.qu_an", "forge.fun.qu_an.", "forge.fun.qu_an.*",
            "foundationgames.enhancedblockentities", "foundationgames.enhancedblockentities.", "foundationgames.enhancedblockentities.*",
            "fuzs.puzzleslib.api", "fuzs.puzzleslib.api.", "fuzs.puzzleslib.api.*",
            "glitchcore.event", "glitchcore.event.", "glitchcore.event.*",
            "guideme.internal.search", "guideme.internal.search.", "guideme.internal.search.*",
            "guideme.internal.shaded", "guideme.internal.shaded.", "guideme.internal.shaded.*",
            "guideme.libs.micromark", "guideme.libs.micromark.", "guideme.libs.micromark.*",
            "guideme.render", "guideme.render.", "guideme.render.*",
            "guideme.style", "guideme.style.", "guideme.style.*",
            "icyllis.modernui.fragment", "icyllis.modernui.fragment.", "icyllis.modernui.fragment.*",
            "icyllis.modernui.markdown", "icyllis.modernui.markdown.", "icyllis.modernui.markdown.*",
            "icyllis.modernui.text", "icyllis.modernui.text.", "icyllis.modernui.text.*",
            "immersive_aircraft.network", "immersive_aircraft.network.", "immersive_aircraft.network.*",
            "immersive_paintings.network", "immersive_paintings.network.", "immersive_paintings.network.*",
            "io.github.douira.glsl_transformer", "io.github.douira.glsl_transformer.", "io.github.douira.glsl_transformer.*",
            "io.github.lounode.eventwrapper", "io.github.lounode.eventwrapper.", "io.github.lounode.eventwrapper.*",
            "io.redspace.ironsspellbooks", "io.redspace.ironsspellbooks.", "io.redspace.ironsspellbooks.*",
            "kotlin.jvm.internal", "kotlin.jvm.internal.", "kotlin.jvm.internal.*",
            "kotlin.reflect.jvm", "kotlin.reflect.jvm.", "kotlin.reflect.jvm.*",
            "me.fzzyhmstrs.fzzy_config", "me.fzzyhmstrs.fzzy_config.", "me.fzzyhmstrs.fzzy_config.*",
            "me.lucko.spark.common", "me.lucko.spark.common.", "me.lucko.spark.common.*",
            "me.lucko.spark.forge", "me.lucko.spark.forge.", "me.lucko.spark.forge.*",
            "me.lucko.spark.lib", "me.lucko.spark.lib.", "me.lucko.spark.lib.*",
            "me.lucko.spark.lib.adventure", "me.lucko.spark.lib.adventure.", "me.lucko.spark.lib.adventure.*",
            "me.lucko.spark.lib.bytebuddy", "me.lucko.spark.lib.bytebuddy.", "me.lucko.spark.lib.bytebuddy.*",
            "me.lucko.spark.proto", "me.lucko.spark.proto.", "me.lucko.spark.proto.*",
            "me.paulf.fairylights", "me.paulf.fairylights.", "me.paulf.fairylights.*",
            "me.shedaniel.cloth", "me.shedaniel.cloth.", "me.shedaniel.cloth.*",
            "me.srrapero720.chloride", "me.srrapero720.chloride.", "me.srrapero720.chloride.*",
            "me.team.creative.creativecore", "me.team.creative.creativecore.", "me.team.creative.creativecore.*",
            "mezz.jei.common", "mezz.jei.common.", "mezz.jei.common.*",
            "mekanism.client.render", "mekanism.client.render.", "mekanism.client.render.*",
            "mekanism.common.item", "mekanism.common.item.", "mekanism.common.item.*",
            "mekanism.common.lib", "mekanism.common.lib.", "mekanism.common.lib.*",
            "mekanism.generators.client", "mekanism.generators.client.", "mekanism.generators.client.*",
            "mekanism.tools.client", "mekanism.tools.client.", "mekanism.tools.client.*",
            "net.covers1624.quack", "net.covers1624.quack.", "net.covers1624.quack.*",
            "net.creeperhost.ftbbackups", "net.creeperhost.ftbbackups.", "net.creeperhost.ftbbackups.*",
            "net.creeperhost.ftbbackups.repack", "net.creeperhost.ftbbackups.repack.", "net.creeperhost.ftbbackups.repack.*",
            "net.creeperhost.polylib", "net.creeperhost.polylib.", "net.creeperhost.polylib.*",
            "net.enderio.core.common", "net.enderio.core.common.", "net.enderio.core.common.*",
            "net.irisshaders.iris", "net.irisshaders.iris.", "net.irisshaders.iris.*",
            "net.mehvahdjukaar.amendments", "net.mehvahdjukaar.amendments.", "net.mehvahdjukaar.amendments.*",
            "net.mehvahdjukaar.moonlight", "net.mehvahdjukaar.moonlight.", "net.mehvahdjukaar.moonlight.*",
            "net.mehvahdjukaar.supplementaries", "net.mehvahdjukaar.supplementaries.", "net.mehvahdjukaar.supplementaries.*",
            "net.montoyo.wd", "net.montoyo.wd.", "net.montoyo.wd.*",
            "net.mrqx.sbr_core", "net.mrqx.sbr_core.", "net.mrqx.sbr_core.*",
            "net.p3pp3rf1y.sophisticatedbackpacks", "net.p3pp3rf1y.sophisticatedbackpacks.", "net.p3pp3rf1y.sophisticatedbackpacks.*",
            "net.p3pp3rf1y.sophisticatedcore", "net.p3pp3rf1y.sophisticatedcore.", "net.p3pp3rf1y.sophisticatedcore.*",
            "net.p3pp3rf1y.sophisticatedstorage", "net.p3pp3rf1y.sophisticatedstorage.", "net.p3pp3rf1y.sophisticatedstorage.*",
            "net.potionstudios.biomeswevegone", "net.potionstudios.biomeswevegone.", "net.potionstudios.biomeswevegone.*",
            "net.satisfy.brewery", "net.satisfy.brewery.", "net.satisfy.brewery.*",
            "net.zepalesque.redux", "net.zepalesque.redux.", "net.zepalesque.redux.*",
            "org.antarcticgardens.newage", "org.antarcticgardens.newage.", "org.antarcticgardens.newage.*",
            "org.cef.handler", "org.cef.handler.", "org.cef.handler.*",
            "org.cef.network", "org.cef.network.", "org.cef.network.*",
            "org.embeddedt.modernfix", "org.embeddedt.modernfix.", "org.embeddedt.modernfix.*",
            "org.moddingx.libx", "org.moddingx.libx.", "org.moddingx.libx.*",
            "org.openzen.zenscript.codemodel", "org.openzen.zenscript.codemodel.", "org.openzen.zenscript.codemodel.*",
            "org.openzen.zenscript.javabytecode", "org.openzen.zenscript.javabytecode.", "org.openzen.zenscript.javabytecode.*",
            "org.openzen.zenscript.javashared", "org.openzen.zenscript.javashared.", "org.openzen.zenscript.javashared.*",
            "org.openzen.zenscript.lexer", "org.openzen.zenscript.lexer.", "org.openzen.zenscript.lexer.*",
            "org.openzen.zenscript.parser", "org.openzen.zenscript.parser.", "org.openzen.zenscript.parser.*",
            "org.violetmoon.quark", "org.violetmoon.quark.", "org.violetmoon.quark.*",
            "org.violetmoon.zetaimplforge", "org.violetmoon.zetaimplforge.", "org.violetmoon.zetaimplforge.*",
            "ovh.corail.tombstone", "ovh.corail.tombstone.", "ovh.corail.tombstone.*",
            "plus.dragons.createcentralkitchen", "plus.dragons.createcentralkitchen.", "plus.dragons.createcentralkitchen.*",
            "slimeknights.mantle.data", "slimeknights.mantle.data.", "slimeknights.mantle.data.*",
            "slimeknights.tconstruct.shared", "slimeknights.tconstruct.shared.", "slimeknights.tconstruct.shared.*",
            "slimeknights.tconstruct.tools", "slimeknights.tconstruct.tools.", "slimeknights.tconstruct.tools.*",
            "team.creative.ambientsounds", "team.creative.ambientsounds.", "team.creative.ambientsounds.*",
            "team.creative.creativecore", "team.creative.creativecore.", "team.creative.creativecore.*",
            "teamrazor.deepaether", "teamrazor.deepaether.", "teamrazor.deepaether.*",
            "terrablender.worldgen", "terrablender.worldgen.", "terrablender.worldgen.*",
            "twilightforest.block", "twilightforest.block.", "twilightforest.block.*",
            "twilightforest.block.entity", "twilightforest.block.entity.", "twilightforest.block.entity.*",
            "twilightforest.client", "twilightforest.client.", "twilightforest.client.*",
            "twilightforest.client.particle", "twilightforest.client.particle.", "twilightforest.client.particle.*",
            "twilightforest.client.renderer", "twilightforest.client.renderer.", "twilightforest.client.renderer.*",
            "twilightforest.entity.ai", "twilightforest.entity.ai.", "twilightforest.entity.ai.*",
            "twilightforest.world.components", "twilightforest.world.components.", "twilightforest.world.components.*",
            "twilightforest.world.gen", "twilightforest.world.gen.", "twilightforest.world.gen.*",
            "vazkii.botania.api", "vazkii.botania.api.", "vazkii.botania.api.*",
            "vazkii.botania.client", "vazkii.botania.client.", "vazkii.botania.client.*",
            "vazkii.botania.common", "vazkii.botania.common.", "vazkii.botania.common.*",
            "vazkii.botania.forge", "vazkii.botania.forge.", "vazkii.botania.forge.*",
            "vazkii.botania.mixin", "vazkii.botania.mixin.", "vazkii.botania.mixin.*",
            "vazkii.patchouli.client", "vazkii.patchouli.client.", "vazkii.patchouli.client.*",
            "xaero.common.message", "xaero.common.message.", "xaero.common.message.*",
            "xaero.map.message", "xaero.map.message.", "xaero.map.message.*",
            "me.zeroeightsix.fabric.*"

    );
    private static final Set<String> DETECTED_MODS = new HashSet<>();
    private static final String PROTOCOL = "1.0";
    /*
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("mpem", "config_sync"), // ✅ 使用 new 关键字
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );*/
    // 完整的作弊模组特征库
    private static final Map<String, String> CHEAT_MODS = new LinkedHashMap<String, String>() {{
        // 功能作弊模组
        put("baritone", "自动化作弊 (Baritone)");
        put("freecam", "无碰撞箱，可以间接X-ray (FreeCam)");
        put("worlddownloader", "地图下载 (WorldDownloader)");
        put("tweakeroo", "自由视角 (Tweakeroo)");
        put("itemscroller", "快速物品转移 (ItemScroller)");

        // 黑客客户端
        put("wurst", "Wurst黑客客户端");
        put("aristois", "Aristois客户端");
        put("meteor", "Meteor客户端");
        put("impact", "Impact客户端");

        // 透视/资源
        put("journeymap", "全图透视 (JourneyMap)");
        put("xray", "X-Ray透视");
        put("litematica", "结构透视 (Litematica)");

        // 战斗作弊
        put("killaura", "自动攻击 (KillAura)");
        put("reach", "攻击距离修改 (Reach Mod)");
        put("autocrit", "自动暴击 (AutoCriticals)");
        put("triggerbot", "自动瞄准 (TriggerBot)");

        // 自动化
        put("autofish", "自动钓鱼 (AutoFish)");
        put("carpet", "假人自动化 (Carpet Mod)");
        put("autoclicker", "自动点击器");

        // 反检测
        put("nochatreports", "聊天举报屏蔽 (NoChatReports)");
        put("fabricatedchat", "伪造聊天消息 (Fabricated Chat)");
        put("vanish", "假隐身 (Vanish Mod)");
    }};
    // 类名关键词检测
    private static final List<String> CLASS_KEYWORDS = Arrays.asList(
            "cheat", "hack", "exploit", "reach", "noclip",
            "freecam", "autoclick", "killaura", "xray", "baritone",
            "flyhack", "speedhack", "esp", "aimbot", "triggerbot", "wurst", "aristois", "meteor", "impact", "downloader",
            "baritone", "litematica", "killaura", "autoclicker"
    );
    // 资源包特征
    private static final List<String> RESOURCE_PATTERNS = Arrays.asList(
            "wurst\\.png$", "meteor/", "impact/",
            "killaura\\.json$", "xray/.*\\.png", "baritone/.*\\.png"
    );
    // 检测结果状态
    public static int DETECTION_RESULT = 0;
    // 客户端存储的配置状态
    private static boolean clientDetectionEnabled = false;

    // 初始化注册
    public static void init() {
        // 注册数据包处理器
        /*
        CHANNEL.registerMessage(0, ConfigPacket.class,
                ConfigPacket::encode,
                ConfigPacket::new,
                ConfigPacket::handle
        );*/
    }
    // 豁免列表（非作弊模组）

    // 玩家登录时同步服务器配置
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            /*
            CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ConfigPacket(CoolConfig.isDetectionEnabled())
            );*/
            log("向玩家 " + player.getName().getString() + " 发送安全配置");
        }
    }

    // 安全的主检测入口（仅客户端）
    @OnlyIn(Dist.CLIENT)
    public static void runClientDetection() {
        DETECTION_RESULT = 0;
        DETECTED_MODS.clear();

        if (!FMLEnvironment.dist.isClient()) {
            logError("尝试在服务端运行客户端检测!");
            return;
        }

        // 如果是单人游戏则不检测
        if (Minecraft.getInstance().isSingleplayer()) {
            log("单人游戏模式，跳过反作弊检测");
            return;
        }

        log("开始客户端安全检测...");
        detectModFiles();
        detectResourcePacks();

// 新增配置检查
        if (clientDetectionEnabled) {
            detectClassNames();
            log("类名检测已执行 (服务器强制要求)");
        } else {
            log("类名检测已跳过 (服务器允许)");
        }


        log("检测完成，结果: " + DETECTION_RESULT);
        if (DETECTION_RESULT == 1) {
            String detected = getDetectedModsString();
            log("检测到的作弊内容: " + detected);
            // 发送检测结果到服务器
            NetworkHandler.sendToServer(new NetworkHandler.DetectionPacket(1, detected));
        } else {
            // 发送安全结果
            NetworkHandler.sendToServer(new NetworkHandler.DetectionPacket(0, "安全"));
        }
    }

    // 模组文件检测
    @OnlyIn(Dist.CLIENT)
    private static void detectModFiles() {
        ModList.get().getMods().forEach(mod -> {
            String modId = mod.getModId().toLowerCase();

            // 检查已知作弊模组
            CHEAT_MODS.keySet().forEach(keyword -> {
                if (modId.contains(keyword)) {
                    // 检查是否在豁免列表中
                    if (EXEMPT_SIGNATURES.stream().anyMatch(exempt -> modId.contains(exempt))) {
                        return;
                    }
                    flagDetection(CHEAT_MODS.get(keyword) + " (" + modId + ")");
                }
            });
        });
    }

    // 类名扫描检测
    @OnlyIn(Dist.CLIENT)
    private static void detectClassNames() {
        try {
            ClassScanner.getLoadedClasses().forEach(className -> {
                // 检查是否在豁免列表中
                if (isExemptClass(className)) {
                    return;
                }

                String lowerName = className.toLowerCase();
                for (String keyword : CLASS_KEYWORDS) {
                    if (lowerName.contains(keyword)) {
                        flagDetection("可疑类: " + className);
                        break;
                    }
                }
            });
        } catch (Exception e) {
            logError("类扫描失败: " + e.getMessage());
        }
    }

    // 新增豁免检查方法
    private static boolean isExemptClass(String className) {
        return EXEMPT_SIGNATURES.stream().anyMatch(className::startsWith);
    }

    // 资源包检测
    @OnlyIn(Dist.CLIENT)
    private static void detectResourcePacks() {
        try {
            Minecraft.getInstance().getResourcePackRepository()
                    .getAvailablePacks()
                    .forEach(pack -> {
                        String packName = pack.getId().toLowerCase();

                        // 检查资源包名称
                        CHEAT_MODS.keySet().forEach(keyword -> {
                            if (packName.contains(keyword)) {
                                if (isExemptClass(packName)) {
                                    return;
                                }
                                flagDetection(CHEAT_MODS.get(keyword) + "资源包");
                            }
                        });

                        // 检查资源包特征
                        RESOURCE_PATTERNS.forEach(pattern -> {
                            if (Pattern.compile(pattern).matcher(packName).find()) {
                                if (isExemptClass(packName)) {
                                    return;
                                }
                                flagDetection("作弊资源包: " + packName);
                            }
                        });
                    });
        } catch (Exception e) {
            logError("资源包检测失败: " + e.getMessage());
        }
    }

    // 获取检测结果描述
    public static String getDetectedModsString() {
        return String.join(", ", DETECTED_MODS);
    }

    // 日志工具
    public static void log(String message) {
        System.out.println("[MPEM-AntiCheat] " + message);
    }

    public static void logError(String error) {
        System.err.println("[MPEM-AntiCheat-ERROR] " + error);
    }

    // 标记检测到作弊
    private static void flagDetection(String reason) {
        DETECTION_RESULT = 1;
        DETECTED_MODS.add(reason);
        log("检测到可疑内容: " + reason);
    }

    public static class ConfigPacket {
        private final boolean detectionEnabled;

        public ConfigPacket(boolean detectionEnabled) {
            this.detectionEnabled = detectionEnabled;
        }

        public ConfigPacket(FriendlyByteBuf buf) {
            this.detectionEnabled = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(detectionEnabled);
        }
/*
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                // 客户端接收并存储配置
                clientDetectionEnabled = detectionEnabled;
                log("接收到服务器安全配置: 类名检测 = " + detectionEnabled);
            });
            ctx.get().setPacketHandled(true);
        }*/
    }
}