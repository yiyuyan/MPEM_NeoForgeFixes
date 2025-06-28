package net.shuyanmc.mpem;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.lang.reflect.Method;

public class EventHandlersTRO {

    // 使用 getEntity() 替代 getPlayer()
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        System.out.println("[TRO] Player logged in: " + player.getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        System.out.println("[TRO] Player logged out: " + player.getName().getString());
    }

    public static void registerOptimizedHandlers() {
        try {
            Method loginMethod = EventHandlersTRO.class.getMethod(
                    "onPlayerLogin",
                    PlayerEvent.PlayerLoggedInEvent.class
            );

            Method logoutMethod = EventHandlersTRO.class.getMethod(
                    "onPlayerLogout",
                    PlayerEvent.PlayerLoggedOutEvent.class
            );

            // 创建优化后的监听器
            EventListener loginListener = EventListenerFactory.createListener(loginMethod, null);
            EventListener logoutListener = EventListenerFactory.createListener(logoutMethod, null);
            // 注册监听器
            MpemMod.registerDynamicListener(
                    PlayerEvent.PlayerLoggedInEvent.class,
                    loginListener,
                    EventPriority.NORMAL,
                    false
            );

            MpemMod.registerDynamicListener(
                    PlayerEvent.PlayerLoggedOutEvent.class,
                    logoutListener,
                    EventPriority.NORMAL,
                    false
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to register optimized event handlers", e);
        }
    }
}