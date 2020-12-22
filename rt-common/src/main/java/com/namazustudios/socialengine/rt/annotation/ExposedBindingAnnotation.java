package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.exception.InternalException;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Defines a module which will be exposed to the scripting engine layer. Replaces {@link Expose#modules()}.
 */
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
     * @return the factory type
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
    interface BindingAnnotationFactory {  Annotation construct(Class<?> cls, ExposedBindingAnnotation annotation); }

    /**
     * Utility class for resolving the binding annotation.
     */
    class Util {

        /**
         * Looks up the {@link BindingAnnotationFactory} and exposes it to the
         *
         * @param annotation
         * @return
         */
        public static Annotation resolve(final Class<?> cls, final ExposedBindingAnnotation annotation) {
            try {
                final var factory = annotation.factory().getConstructor().newInstance();
                return factory.construct(cls, annotation);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new InternalException(e);
            }
        }

    }

}
