package net.shuyanmc.mpem.optimization;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LambdaListenerFactory {
    private static final String INVOKE_METHOD = "invoke";
    private static final MethodType LISTENER_TYPE = MethodType.methodType(void.class, Event.class);

    /**
     * 创建基于Lambda的高效事件监听器
     */
    public static EventListener createListener(Method targetMethod, Object instance) throws Exception {
        validateMethod(targetMethod);

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        boolean isStatic = Modifier.isStatic(targetMethod.getModifiers());

        MethodHandle methodHandle = lookup.unreflect(targetMethod);
        MethodType factoryType = isStatic
                ? MethodType.methodType(EventListener.class)
                : MethodType.methodType(EventListener.class, instance.getClass());

        MethodType targetType = MethodType.methodType(void.class,
                isStatic ? methodHandle.type().parameterType(0) : methodHandle.type().parameterType(1));

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                INVOKE_METHOD,
                factoryType,
                LISTENER_TYPE,
                methodHandle,
                targetType
        );

        try {
            return isStatic
                    ? (EventListener) callSite.getTarget().invoke()
                    : (EventListener) callSite.getTarget().invoke(instance);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void validateMethod(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException("Event handler must have exactly 1 parameter");
        }
        if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new IllegalArgumentException("Parameter must be an Event subclass");
        }
        if (Modifier.isAbstract(method.getModifiers())) {
            throw new IllegalArgumentException("Event handler method cannot be abstract");
        }
    }
}