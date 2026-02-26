package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link Repeatable} container annotation for {@link ElementTypeRequest}.
 */
@Target(PACKAGE)
@Retention(RUNTIME)
public @interface ElementTypeRequests {

    /**
     * The contained {@link ElementTypeRequest} annotations.
     *
     * @return the {@link ElementTypeRequest} annotations
     */
    ElementTypeRequest[] value();

}