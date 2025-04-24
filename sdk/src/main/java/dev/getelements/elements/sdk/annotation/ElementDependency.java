package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Element;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that a particular {@link Element} depends on another {@link Element}.
 */
@Target(PACKAGE)
@Retention(RUNTIME)
@Repeatable(ElementDependencies.class)
public @interface ElementDependency {

    /**
     * The name of the {@link Element}
     * @return the name of the {@link Element}
     */
    String value();

}
