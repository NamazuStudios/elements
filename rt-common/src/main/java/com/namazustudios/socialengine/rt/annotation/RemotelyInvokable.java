package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.RoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Designates a method as being remotely invokable.  The method must be marked in a {@link Proxyable} type, and it must
 * provide a class that implements {@link RoutingStrategy}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemotelyInvokable {
    Class<? extends RoutingStrategy> value() default RoutingStrategy.DefaultRoutingStrategy.class;
}
