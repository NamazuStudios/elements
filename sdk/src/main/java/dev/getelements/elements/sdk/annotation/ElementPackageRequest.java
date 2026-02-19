package dev.getelements.elements.sdk.annotation;

import dev.getelements.elements.sdk.PackageRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Declares a package visibility request on behalf of an Element. Placed on a {@code package-info.java} package
 * declaration, this annotation tells the {@link dev.getelements.elements.sdk.PermittedTypesClassLoader} that
 * the Element needs access to all types within specific packages not otherwise exposed through the system-side
 * {@link dev.getelements.elements.sdk.PermittedTypes} or {@link dev.getelements.elements.sdk.PermittedPackages}
 * rules.
 * </p>
 *
 * <h2>Interaction with PackageRequest</h2>
 * <p>
 * Each annotation instance is paired with a {@link PackageRequest} implementation specified by {@link #request()}.
 * When the classloader evaluates whether to permit a class, it passes the annotation instance and the package name
 * of the class being loaded to {@link PackageRequest#test(Object, Object)}. The annotation serves as the
 * configuration carrier: the {@link #value()} patterns and any other attributes are available to the predicate
 * at evaluation time, allowing a single implementation to be reused across multiple packages with different
 * configurations.
 * </p>
 *
 * <h2>Difference from ElementTypeRequest</h2>
 * <p>
 * {@link ElementTypeRequest} matches against individual binary class names, permitting specific types.
 * {@code @ElementPackageRequest} matches against the containing package name, permitting all classes in the
 * matched package. Use this annotation when you want to expose an entire package namespace to the Element.
 * </p>
 *
 * <h2>Simple Usage</h2>
 * <p>
 * For a fixed set of packages, omit {@link #request()} and list the package names in {@link #value()}.
 * The default {@link PackageRequest.Literal} implementation will permit all classes in exactly those packages:
 * </p>
 * <pre>{@code
 * @ElementPackageRequest(value = {"com.example.shared", "com.example.api"})
 * package com.example.myelement;
 * }</pre>
 *
 * <h2>Pattern-Based Usage</h2>
 * <p>
 * For wildcard or regex package matching, supply a custom {@link PackageRequest} via {@link #request()}:
 * </p>
 * <pre>{@code
 * @ElementPackageRequest(request = PackageRequest.Wildcard.class, value = {"com.example.*"})
 * package com.example.myelement;
 * }</pre>
 *
 * @see PackageRequest
 * @see PackageRequest.Literal
 * @see ElementTypeRequest
 * @see dev.getelements.elements.sdk.PermittedTypesClassLoader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
@Repeatable(ElementPackageRequests.class)
public @interface ElementPackageRequest {

    /**
     * The package names to permit. Used directly by the default {@link PackageRequest.Literal} implementation,
     * and passed through the annotation instance to any custom {@link PackageRequest} for use as it sees fit
     * (e.g., as exact names, wildcard patterns, or regular expressions depending on the {@link #request()} type).
     *
     * @return the package names or patterns to match
     */
    String[] value() default {};

    /**
     * Specifies the {@link PackageRequest} implementation used to evaluate this annotation against a package name.
     * Defaults to {@link PackageRequest.Literal}, which requires an exact package name match.
     *
     * @return the request type
     */
    Class<? extends PackageRequest> request() default PackageRequest.Literal.class;

}