package net.shuyanmc.mpem;

import net.neoforged.bus.ConsumerEventHandler;
import net.neoforged.bus.GeneratedEventListener;
import net.neoforged.bus.SubscribeEventListener;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.shuyanmc.mpem.EventDispatchException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class FastEventHandler extends GeneratedEventListener {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private final MethodHandle methodHandle;
    private final Object target;
    private final Class<?> eventType;

    private FastEventHandler(Object target, MethodHandle methodHandle) {
        super();
        this.target = target;
        this.methodHandle = methodHandle;
        this.eventType = methodHandle.type().parameterType(1);
    }

    public static EventListener create(Object target, Method method) {
        try {
            method.setAccessible(true);
            MethodHandle mh = LOOKUP.unreflect(method);
            return new FastEventHandler(target, mh);
        } catch (IllegalAccessException e) {
            return new ReflectionFallbackHandler(target, method);
        }
    }


    public void invoke(Event event) {
        try {
            if (eventType.isInstance(event)) {
                methodHandle.invoke(target, event);
            }
        } catch (Throwable e) {
            throw new EventDispatchException("Failed to invoke event handler", e);
        }
    }

    private static class ReflectionFallbackHandler extends GeneratedEventListener {
        private final Object target;
        private final Method method;

        ReflectionFallbackHandler(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        @Override
        public void invoke(Event event) {
            try {
                method.invoke(target, event);
            } catch (ReflectiveOperationException e) {
                throw new EventDispatchException("Reflection handler failed", e);
            }
        }
    }
}