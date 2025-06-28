package net.shuyanmc.mpem;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class EventHelper {
    public static final MethodType EVENT_HANDLER_TYPE = 
        MethodType.methodType(void.class, Object.class);
    
    public static void validateMethod(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException("Event handler must have exactly one parameter");
        }
    }
}