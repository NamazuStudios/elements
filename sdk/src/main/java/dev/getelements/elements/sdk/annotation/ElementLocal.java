package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Element;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the Element {@link ClassLoader} to copy the annotated from its parent {@link ClassLoader} and load into the
 * {@link ClassLoader} for the {@link Element} running within it.
 *
 * This annotation is only valid for when an {@link Element} is running in
 * {@link dev.getelements.elements.sdk.ElementType#ISOLATED_CLASSPATH} mode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface ElementLocal {}
