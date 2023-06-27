package dev.getelements.elements.rt.annotation;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Indicates that the annotated type is proxyable.  That means that the interface shall be coded in such a way that the
 * parameters, return types, or other general behavior can be proxied over a network or a cluster of machines.
 *
 * This means that parameters and return types are capable of being serialized (such as {@link Serializable}or can be
 * made into proxies themselves.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxyable {}
