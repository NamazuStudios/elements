package dev.getelements.elements.sdk.cluster.remote.annotation;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.annotation.Disabled;
import dev.getelements.elements.sdk.annotation.ElementServiceReference;
import dev.getelements.elements.sdk.cluster.remote.routing.AddressingStrategy;
import dev.getelements.elements.sdk.cluster.remote.routing.DefaultAddressingStrategy;
import dev.getelements.elements.sdk.cluster.remote.routing.RoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation specifying how the remote address for a {@link RemotelyInvokable} method is determined, either via
 * an {@link AddressingStrategy} or a {@link ServiceLocator}-resolved service.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Addressing {

    /**
     * Specifies the {@link AddressingStrategy} used to determine the remote address. When {@link #service()} is set
     * to anything other than {@link Disabled}, this value will be ignored. The specified strategy here must have a
     * default constructor.
     *
     * @return the strategy type
     */
    Class<? extends AddressingStrategy> value() default DefaultAddressingStrategy.class;

    /**
     * Specifies the {@link RoutingStrategy} used to distribute the remote invocations.  This will be the type resolved
     * against the IoC container from the {@link ServiceLocator}. Note the requested strategy must be available on the
     * element's classloader. If the type {@link Disabled} is specified here, then the method specified in
     * {@link #value()} will be used instead.
     *
     * @return the {@link RoutingStrategy} class
     */
    ElementServiceReference service() default @ElementServiceReference(Disabled.class);

}
