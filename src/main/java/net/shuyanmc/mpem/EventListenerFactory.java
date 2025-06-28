// EventListenerFactory.java
package net.shuyanmc.mpem;


import net.neoforged.bus.GeneratedEventListener;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@AsyncHandler
public final class EventListenerFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Method, EventListener> STATIC_CACHE = new ConcurrentHashMap<>();
    private static final Map<Method, Function<Object, EventListener>> INSTANCE_FACTORY_CACHE = new ConcurrentHashMap<>();

    public static EventListener createListener(Method method, Object instance) {
        if (Modifier.isStatic(method.getModifiers())) {
            return STATIC_CACHE.computeIfAbsent(method, m ->
                    createStaticLambda(m)
            );
        }
        return INSTANCE_FACTORY_CACHE
                .computeIfAbsent(method, EventListenerFactory::createInstanceFactory)
                .apply(instance);
    }

    private static EventListener createStaticLambda(Method method) {
        try {
            MethodHandle target = LOOKUP.unreflect(method);
            return new GeneratedEventListener() {
                @Override
                public void invoke(Event event) {
                    try {
                        target.invoke(event);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create static listener", e);
        }
    }

    private static Function<Object, EventListener> createInstanceFactory(Method method) {
        try {
            MethodHandle target = LOOKUP.unreflect(method);
            return instance -> new GeneratedEventListener() {
                @Override
                public void invoke(Event event) {
                    try {
                        target.bindTo(instance).invoke(event);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create instance factory", e);
        }
    }
}