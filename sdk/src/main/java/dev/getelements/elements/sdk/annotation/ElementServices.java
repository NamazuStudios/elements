package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.*;

/**
 * {@link Repeatable} type for {@link ElementService}.
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementServices {

    ElementService[] value() default {};

}
