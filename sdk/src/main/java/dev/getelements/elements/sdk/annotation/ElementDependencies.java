package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link Repeatable} annotation for {@link ElementDependency}.
 */
@Target(PACKAGE)
@Retention(RUNTIME)
public @interface ElementDependencies {

    /**
     * The {@link ElementDependency} to be used when creating the Elements.
     * @return the {@link ElementDependency} types to be used when creating the element.
     */
    ElementDependency[] value();

}
