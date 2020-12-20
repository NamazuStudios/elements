package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.*;
import java.util.function.Supplier;

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
     * The binding annotation to use when exposing the module.
     *
     * @return the binding annotation
     */
    Class<? extends Annotation> annotation() default Undefined.class;

    /**
     * Indicates that importing this particular module is deprecated.
     *
     * @return true if deprecated, false otherwise
     */
    DeprecationDefinition deprecated() default @DeprecationDefinition("");

    /**
     * Annotation type which indicates that no annotation type is defined.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Undefined {}

}
