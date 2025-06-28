package net.shuyanmc.mpem;


import net.neoforged.bus.api.IEventBus;
import net.shuyanmc.mpem.events.DatabaseQueryEvent;

public class EventHandlers {
    public static void register(IEventBus modEventBus, IEventBus forgeEventBus) {
        // 注册异步事件处理器
        new DatabaseQueryEvent("", false).registerToBus(forgeEventBus);

    }
}