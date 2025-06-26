package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.Element;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotates the type as a Service Provider Interface (SPI) implementation. This is a hint to the system that the
 * type should be included in the scanning process when loading {@link Element}s.
 */
@Target({TYPE, PACKAGE})
public @interface ElementSpiImplementation {}
