package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExposedBindingAnnotation {

    /**
     * The binding annotation to use when exposing the module.
     *
     * @return the binding annotation
     */
    Class<? extends Annotation> value() default Undefined.class;

    /**
     * Specifies a type which is used to construct the actual binding annotation.
     *
     * @return
     */
    Class<? extends BindingAnnotationFactory> factory() default DefaultBindingAnnotationFactory.class;

    /**
     * Annotation type which indicates that no annotation type is defined.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Undefined {}

    /**
     * Resolves an instance of {@link Annotation} given the supplied {@link ExposedModuleDefinition}
     */
    @FunctionalInterface
    interface BindingAnnotationFactory {  Annotation construct(ExposedBindingAnnotation definition); }

}
