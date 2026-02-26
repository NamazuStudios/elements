package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link Repeatable} container annotation for {@link ElementPackageRequest}.
 */
@Target(PACKAGE)
@Retention(RUNTIME)
public @interface ElementPackageRequests {

    /**
     * The contained {@link ElementPackageRequest} annotations.
     *
     * @return the {@link ElementPackageRequest} annotations
     */
    ElementPackageRequest[] value();

}