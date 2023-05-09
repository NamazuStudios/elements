package dev.getelements.elements.rt.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Hides a particular type from any kind of exposure to scripting languages.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Private {}
