package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a type or all types within a package are to be considered public and exposed to all other elements. For
 * the sake of performance and convenience, the {@link dev.getelements.elements.sdk.ElementLoaderFactory} will only
 * observe this annotation when it is applied to the package-info class of a package.
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface ElementPublic {}
