package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.ServiceLocator;

import java.lang.annotation.*;

/**
 * Exports the target type making it available in the {@link ServiceLocator} methods. Specifically, this means that
 * when calling methods such as {@link ServiceLocator#findInstance(Class)}, the type must bear this annotation or
 * it must be specified via {@link ElementService#export()} annotation. Note, this does not specify an implementation
 * and should be specified separately using the {@link ElementServiceImplementation} annotation.
 */
@Repeatable(ElementServiceExports.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementServiceExport {

    /**
     * Gets a list of all exposed which this service exposes in addition to the specified type. This can be useful if,
     * for example, an interface implements multiple interfaces which should be exposed separately. If left empty, then
     * only the annotated type (or type specified in {@link ElementService#value()}) will be exposed.
     *
     * If specified, this will expose only the types explicitly mentioned in the value.
     *
     * @return the target type
     */
    Class<?>[] value() default {};

    /**
     * Specifies the name of the service. If blank, the service will be unnamed.
     *
     * @return the name of the service.
     */
    String name() default "";

    /**
     * Controls whether this export is exposed to the parent Guice injector when running in
     * shared-classpath mode. Set to {@code false} for element-lifecycle types (e.g.
     * {@code jakarta.ws.rs.core.Application}) that are consumed through each element's own
     * {@link dev.getelements.elements.sdk.ServiceLocator} and must not be exposed to the shared
     * parent injector (which would cause a "bound twice" error when multiple elements in the same
     * deployment each export the same type).
     *
     * @return true if this export should be exposed to the parent injector, false otherwise
     */
    boolean expose() default true;

}
