package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.*;

/**
 * Defines an exposed module. Used within the {@link Expose} annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDefinition {

    /**
     * The name of the module to expose.
     *
     * @return the name.
     */
    String value();

    /**
     * When binding to the underlying language, this defines the case format
     *
     *
     * @return the method case format
     */
    CodeStyle style() default @CodeStyle();

    /**
     * Specifies the binding {@link Annotation} for the module.
     *
     * @return the {@link ExposedBindingAnnotation}
     */
    ExposedBindingAnnotation annotation() default @ExposedBindingAnnotation();

    /**
     * Indicates that importing this particular module is deprecated.
     *
     * @return true if deprecated, false otherwise
     */
    DeprecationDefinition deprecated() default @DeprecationDefinition(deprecated = false);

}
