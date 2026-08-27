package dev.getelements.elements.sdk.cluster.remote.annotation;

import dev.getelements.elements.sdk.cluster.id.HasNodeId;
import dev.getelements.elements.sdk.cluster.remote.routing.DefaultRoutingStrategy;
import dev.getelements.elements.sdk.cluster.remote.routing.RoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a parameter which is the source of routing. In almost all cases, the parameter must be of type
 * {@link HasNodeId} indicating that the request should be routed to a particular node. Ultimately, it is up to the
 * associated {@link RoutingStrategy} to handle how this parameter is parsed. The {@link DefaultRoutingStrategy} will
 * use {@link HasNodeId} to determine routing.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoutingAddressSource {}