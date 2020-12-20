package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecationDefinition {

    /**
     * Indicates that the module is deprecated. Supplying a value will be relayed as the deprecation reason to the code
     * which uses the deprecated module. If this string is blank, the value is not deprecated.
     *
     * @return the deprecation message
     */
    String value();

}
