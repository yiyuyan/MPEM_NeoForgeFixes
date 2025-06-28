package net.shuyanmc.mpem.util;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber
public class EntityCleaner {
    private static long lastCleanTime = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        //if (event.phase == TickEvent.Phase.END) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime - lastCleanTime > 10000) { // 每游戏日(10分钟)执行
            lastCleanTime = gameTime;
            cleanMostFrequentMonsters((MinecraftServer) event.getServer());
        }
        //}
    }

    private static void cleanMostFrequentMonsters(MinecraftServer server) {
        ServerLevel level = server.overworld();
        long gameTime = server.getTickCount();
        Map<Class<? extends Monster>, List<Monster>> monsterMap = new HashMap<>();

        // 为避免变量重名问题，将 lambda 表达式中的参数名改为 newLevel
        server.getAllLevels().forEach(newLevel -> {
            java.util.stream.StreamSupport.stream(newLevel.getAllEntities().spliterator(), false)
                    .filter(e -> e instanceof Monster)
                    .map(e -> (Monster) e)
                    .forEach(monster -> {
                        monsterMap.computeIfAbsent(monster.getClass(), k -> new ArrayList<>()).add(monster);
                    });
        });

        monsterMap.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .ifPresent(entry -> {
                    entry.getValue().stream()
                            .skip(50) // 保留前50个
                            .forEach(Monster::discard);
                });
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("killmobs")
                        .requires(source -> source.hasPermission(2) || (source.getPlayer() != null
// 检查玩家是否存在，避免空指针异常
                                && source.getPlayer() != null && source.getPlayer().level() != null))
                        .executes(context -> {
                            int[] count = {0};
                            context.getSource().getServer().getAllLevels().forEach(level -> {
                                java.util.stream.StreamSupport.stream(level.getAllEntities().spliterator(), false)
                                        .filter(e -> e instanceof Monster)
                                        .forEach(e -> {
                                            e.discard();
                                            count[0]++;
                                        });
                            });
                            context.getSource().sendSuccess(() -> Component.literal("已清除 " + count[0] + " 个怪物实体"), true);
                            return count[0];
                        }));
// 根据要求删除多余的右括号，此处不添加额外代码
    }
}