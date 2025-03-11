package dev.getelements.elements.sdk.annotation;


import java.lang.annotation.*;

/**
 * Defines a service provided by the element. This annotation must accompany the same {@link Package} which bears the
 * {@link ElementDefinition}, or else it will be ignored.
 *
 * If the {@link ElementServiceExport} appears directly on a type, then this does not need to appear on the package
 * definition as well. This is useful for when a element may export services in third-party libraries where it may not
 * be able to modify the original code. (eg, when the database element exposes the raw JDBC object it uses).
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ElementServices.class)
public @interface ElementService {

    /**
     * The service interface type.
     *
     * @return the implementation type
     */
    Class<?> value();

    /**
     * Specifies the {@link ElementServiceExport} for this particualr {@link ElementService}.
     *
     * @return the {@link ElementServiceExport}
     */
    ElementServiceExport export() default @ElementServiceExport();

    /**
     * Specifies the {@link ElementServiceImplementation} for the type.
     *
     * @return the implementation specification
     */
    ElementServiceImplementation implementation() default @ElementServiceImplementation();

}
