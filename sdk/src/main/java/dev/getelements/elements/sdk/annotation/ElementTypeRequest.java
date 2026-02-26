package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.TypeRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Declares a type visibility request on behalf of an Element. Placed on a {@code package-info.java} package
 * declaration, this annotation tells the {@link dev.getelements.elements.sdk.PermittedTypesClassLoader} that
 * the Element needs access to types not otherwise exposed through the system-side
 * {@link dev.getelements.elements.sdk.PermittedTypes} or {@link dev.getelements.elements.sdk.PermittedPackages}
 * rules.
 * </p>
 *
 * <h2>Interaction with TypeRequest</h2>
 * <p>
 * Each annotation instance is paired with a {@link dev.getelements.elements.sdk.TypeRequest} implementation
 * specified by {@link #request()}. When the classloader evaluates whether to permit a class, it passes the
 * annotation instance and the binary class name to
 * {@link dev.getelements.elements.sdk.TypeRequest#test(Object, Object)}. This means the annotation serves
 * as the configuration carrier: the {@link #value()} list and any other annotation attributes are available
 * to the predicate at evaluation time, allowing a single implementation to be reused across multiple packages
 * with different configurations.
 * </p>
 *
 * <h2>Simple Usage</h2>
 * <p>
 * For a fixed set of types, omit {@link #request()} and list the binary class names in {@link #value()}.
 * The default {@link dev.getelements.elements.sdk.TypeRequest.Literal} implementation will permit exactly
 * those names:
 * </p>
 * <pre>{@code
 * @ElementTypeRequest(value = {"com.example.shared.Foo", "com.example.shared.Bar"})
 * package com.example.myelement;
 * }</pre>
 *
 * <h2>Custom TypeRequest</h2>
 * <p>
 * For dynamic or pattern-based matching, supply a custom {@link dev.getelements.elements.sdk.TypeRequest}
 * via {@link #request()}. The {@link #value()} array is still passed through the annotation instance
 * and can be used by the implementation however it sees fit (e.g., as prefixes, patterns, or ignored entirely):
 * </p>
 * <pre>{@code
 * @ElementTypeRequest(request = PrefixTypeRequest.class, value = "com.example.shared.")
 * package com.example.myelement;
 * }</pre>
 *
 * @see dev.getelements.elements.sdk.TypeRequest
 * @see dev.getelements.elements.sdk.TypeRequest.Literal
 * @see dev.getelements.elements.sdk.PermittedTypesClassLoader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
@Repeatable(ElementTypeRequests.class)
public @interface ElementTypeRequest {

    /**
     * The binary names of the types to permit. Used directly by the default {@link dev.getelements.elements.sdk.TypeRequest.Literal}
     * implementation, and passed through the annotation instance to any custom {@link dev.getelements.elements.sdk.TypeRequest}
     * for use as it sees fit.
     *
     * @return the binary names of the permitted types
     */
    String[] value() default {};

    /**
     * Specifies the request type.
     *
     * @return the request type
     */
    Class<? extends TypeRequest> request() default TypeRequest.Literal.class;

}
