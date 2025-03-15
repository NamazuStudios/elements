package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a java package to include in the element scanning process.
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface ElementPackage {

    /**
     * The name of the Java package.
     *
     * @return the package
     */
    String value() default "";

    /**
     * Set to true to indicate that subpackages should be scanned.
     * 
     * @return true to scan subpackages
     */
    boolean recursive() default false;

}
