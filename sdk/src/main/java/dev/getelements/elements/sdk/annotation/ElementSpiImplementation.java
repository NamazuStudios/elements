package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Element;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates the type as a Service Provider Interface (SPI) implementation. This is a hint to the system that the
 * type should be included in the scanning process when loading {@link Element}s.
 *
 * Starting with Elements. 3.3.0, this annotation is used to mark classes that implement a Service Provider Interface
 * (SPI) because the actual SPI must be provided by the Element instead of by the core system. This allows for greater
 * flexibility and modularity in the Elements framework, enabling developers to define their own SPI implementations
 * or use our own provided ones.
 *
 * Currently only Guice 7.0.X is supported officially.
 * @since 3.3.0
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ElementSpiImplementation {

    /**
     * Specifies the dependencies of this SPI implementation. This is used to ensure that the required elements are
     * loaded before this SPI implementation is used.
     *
     * @return an array of {@link ElementPackage} dependencies
     */
    ElementPackage[] dependencies() default {};

}
