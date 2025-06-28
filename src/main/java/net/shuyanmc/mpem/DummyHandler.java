// DummyHandler.java
package net.shuyanmc.mpem;


import net.neoforged.bus.GeneratedEventListener;
import net.neoforged.bus.api.Event;

public final class DummyHandler extends GeneratedEventListener {
    public static final DummyHandler INSTANCE = new DummyHandler();

    @AsyncHandler
    @Override
    public void invoke(Event event) {
        // 空实现，实际逻辑已被替换
    }
}