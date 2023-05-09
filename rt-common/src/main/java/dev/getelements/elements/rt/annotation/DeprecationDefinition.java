package dev.getelements.elements.rt.annotation;

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
     * which uses the deprecated module. The warning should appear in logs when attempting to use the module.
     *
     * @return the deprecation message
     */
    String value() default "<deprecated>";

    /**
     * Flag to indicate explicit deprecation. It should rarely be necessary to explicitly set this flag.
     *
     * @return true if deprecated, false otherwise
     */
    boolean deprecated() default true;

}
