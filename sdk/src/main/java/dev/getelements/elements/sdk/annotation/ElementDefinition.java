package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a {@link Element}. This must be at the package level and will, by default, scan
 * all sub-packages for types annotated with {@link ElementService} and exposes them via the {@link ElementLoader}
 * interface.
 *
 * A element may not necessarily need to provide services, so this annotation is not strictly required anywhere. However,
 * no types may be instantiated using {@link ElementLoader} without this present.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface ElementDefinition {

    /**
     * Names the Element. If left unspecified, the element will bear the name of the package.
     *
     * @return the name of the element.
     */
    String value() default "";

    /**
     * Specifies additional packages to scan when locating Element components.
     *
     * @return a listing of additional element packages.
     */
    ElementPackage[] additionalPackages() default {};

    /**
     * Set to true to scan this package and all subordinate packages for services.
     */
    boolean recursive() default false;

    /**
     * Specifies the {@link ElementLoader} used to load this element. If left blank, this will use the default element
     * loader specified in the Java SPI system. If an {@link Element} requires special loading semantics that are not
     * provided by the default annotation-driven schemes, then this allows the custom loader to be used.
     *
     * @return the {@link ElementLoader} type
     */
    Class<? extends ElementLoader> loader() default ElementLoader.Default.class;

}
