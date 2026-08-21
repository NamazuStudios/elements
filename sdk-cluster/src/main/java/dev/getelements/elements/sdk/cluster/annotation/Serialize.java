package dev.getelements.elements.sdk.cluster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a method parameters to be serialized for remote invocation.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Serialize {

    /**
     * The name of the parameter. If left a blank string, then the name will be inferred by the
     *
     * @return the name of the parameter
     */
    String value() default "";

}
