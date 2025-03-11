package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.ElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an implementation of a service. When placed directly on the type itself, the implementation will be the
 * type itself. When used in the {@link ElementService} annotation, then this type's {@link #value()} will be used to
 * determine the implementation.
 *
 * The {@link ElementLoader} SPI implementation will determine exactly how implementations handle the
 * {@link DefaultImplementation} case. However, where the specification is explicit, the implementation must supply the
 * type specified. Additionally, when placed directly on a type (as opposed to appearing in
 * {@link ElementService#implementation()}, the loader must use that type unless otherwise specified.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementServiceImplementation {

    /**
     * Specifies the implementation.
     *
     * @return the value
     */
    Class<?> value() default DefaultImplementation.class;

    /**
     * Indicates if this implementation should be exposed as well.
     *
     * @return
     */
    boolean expose() default false;

    /**
     * A marker class used as a placeholder to indicate that there is no specified implementation.
     */
    class DefaultImplementation { private DefaultImplementation() {} }

}
