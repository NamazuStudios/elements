package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a method as being remotely invokable.  The method must be marked in a {@link Proxyable} type, and it may
 * provide {@link Routing} information if desired.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemotelyInvokable {

    /**
     * Specifies the routing options using the {@link Routing} annotation.
     *
     * @return the {@link Routing}
     */
    Routing routing() default @Routing;

}
