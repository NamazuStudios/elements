package com.namazustudios.socialengine.rt.annotation;

import com.google.common.base.CaseFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

/**
 * Indicates the exposed code style.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeStyle {

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat methodCaseFormat() default LOWER_UNDERSCORE;

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method parameter bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat parameterCaseFormat() default LOWER_UNDERSCORE;

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method parameter bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat constantCaseFormat() default UPPER_UNDERSCORE;

}
