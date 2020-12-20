package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.*;

/**
 * Defines an exposed module. Used within the {@link Expose} annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExposedModuleDefinition {

    /**
     * The name of the module to expose.
     *
     * @return the name.
     */
    String value();

    /**
     * Indicates that importing this particular module is deprecated.
     *
     * @return true if deprecated, false otherwise
     */
    DeprecationDefinition deprecated() default @DeprecationDefinition("");

    /**
     * Specifies the binding {@link Annotation} for the module.
     *
     * @return the {@link ExposedBindingAnnotation}
     */
    ExposedBindingAnnotation annotation() default @ExposedBindingAnnotation();

}
