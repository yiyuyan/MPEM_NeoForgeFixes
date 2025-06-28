package net.shuyanmc.mpem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记事件处理方法为异步执行
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.METHOD})
public @interface AsyncHandler {
    /**
     * 线程优先级 (默认为NORM_PRIORITY)
     */
    int priority() default Thread.NORM_PRIORITY;

}