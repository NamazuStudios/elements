package dev.getelements.elements.sdk.cluster.remote.annotation;

import dev.getelements.elements.sdk.ServiceLocator;

/**
 * Specifies how a type referenced by a remoting annotation (such as an
 * {@link dev.getelements.elements.sdk.cluster.remote.routing.AddressingStrategy} or
 * {@link dev.getelements.elements.sdk.cluster.remote.routing.RoutingStrategy}) should be instantiated.
 */
public enum InstantiationStrategy {
    /**
     * Construct the type using its default (no-argument) constructor.
     */
    DEFAULT_CONSTRUCTOR,

    /**
     * Construct the type using a {@link ServiceLocator}
     */
    ELEMENT_SERVICE_LOCATOR
}
