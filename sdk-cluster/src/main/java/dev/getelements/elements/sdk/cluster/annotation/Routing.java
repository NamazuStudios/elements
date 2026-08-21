package dev.getelements.elements.sdk.cluster.annotation;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.cluster.routing.RoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the routing information for the method call.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Routing {

    /**
     * Specifies the {@link RoutingStrategy} used to distribute the remote invocations.  This will be the type resolved
     * against the IoC container from the {@link ServiceLocator}.
     *
     * @return the {@link RoutingStrategy} class
     */
    Class<? extends RoutingStrategy> value() default DefaultRoutingStrategy.class;

    /**
     * Optionally specifies the name of the {@link RoutingStrategy} to use.  If non-empty, this will be used in
     * conjunction with the {@link ServiceLocator} to fetch the {@link RoutingStrategy} from the container.
     *
     * @return the name
     */
    String name() default "";

}
