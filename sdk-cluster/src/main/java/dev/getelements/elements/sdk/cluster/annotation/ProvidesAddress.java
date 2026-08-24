package dev.getelements.elements.sdk.cluster.annotation;

import dev.getelements.elements.sdk.cluster.routing.RoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a method parameter as capable of providing a routing address. Only one parameter may bear this annotation
 * and it will be used with the corresponding {@link RoutingStrategy}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProvidesAddress {}
