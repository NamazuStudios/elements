package dev.getelements.elements.sdk.cluster.remote.annotation;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.annotation.ElementServiceReference;
import dev.getelements.elements.sdk.cluster.remote.routing.DefaultRoutingStrategy;
import dev.getelements.elements.sdk.cluster.remote.routing.RoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.getelements.elements.sdk.cluster.remote.annotation.InstantiationStrategy.DEFAULT_CONSTRUCTOR;

/**
 * Specifies the routing information for the method call.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Routing {

    /**
     * Specifies the {@link RoutingStrategy} class used to route the remote invocation. The specified strategy must
     * have a default constructor.
     *
     * @return the {@link RoutingStrategy} class
     */
    Class<? extends RoutingStrategy> value() default DefaultRoutingStrategy.class;

    /**
     * Specifies the {@link RoutingStrategy} used to distribute the remote invocations.  This will be the type resolved
     * against the IoC container from the {@link ServiceLocator}. Note the requested strategy must be available on the
     * element's classloader.
     *
     * @return the {@link RoutingStrategy} class
     */
    ElementServiceReference service() default @ElementServiceReference(DefaultRoutingStrategy.class);

}
