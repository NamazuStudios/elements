package dev.getelements.elements.rt.annotation;

import dev.getelements.elements.rt.exception.InternalException;

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
     * Resolves an instance of {@link Annotation} given the supplied {@link ModuleDefinition}
     */
    @FunctionalInterface
    interface BindingAnnotationFactory {

        default Annotation construct(Class<?> cls, ExposedBindingAnnotation annotation) {
            return construct(cls, annotation.value());
        }

        Annotation construct(Class<?> cls, Class<? extends Annotation> annotationClass);

    }

    /**
     * Utility class for resolving the binding annotation.
     */
    class Util {

        /**
         * Looks up the {@link BindingAnnotationFactory}, instantiates it, and then applies it to the supplied
         * {@link Class<?>}. This returns a binding annotation which can be automatically integrated into an IoC
         * container.
         *
         * @param annotation the {@link ExposedBindingAnnotation} annotation
         * @return the {@link Annotation} which matches the
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
