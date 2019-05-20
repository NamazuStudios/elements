package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.RoutingAddressProvider;
import com.namazustudios.socialengine.rt.RoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Designates a method parameter as capable of providing a routing address. It is expected that the annotated parameter
 * is a {@link RoutingAddressProvider}, and that at most one field in the method parameters should be annotated.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddressProvider {
}
