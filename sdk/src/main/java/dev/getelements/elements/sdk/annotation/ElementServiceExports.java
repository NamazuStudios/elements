package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link java.lang.annotation.Repeatable} type for {@link ElementServiceExport}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementServiceExports {

    ElementServiceExport[] value();

}
