package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementPackageRequest;

import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * An Element-declared predicate that requests access to all types within specific packages not otherwise permitted
 * by the system-side {@link PermittedTypes} or {@link PermittedPackages} visibility rules. Where
 * {@link PermittedPackages} is registered by the host system via {@link java.util.ServiceLoader} and governs
 * which packages the system chooses to expose, {@code PackageRequest} is declared by the Element itself,
 * allowing it to assert that it needs access to an entire package at load time.
 * </p>
 *
 * <h2>How It Works</h2>
 * <p>
 * Implementations are referenced from the {@link ElementPackageRequest} annotation, which is placed on a package
 * declaration ({@code package-info.java}) within the Element. When the {@link PermittedTypesClassLoader} evaluates
 * a class for visibility, it discovers any {@code PackageRequest} implementations declared by the Element and
 * invokes {@link #test(Object, Object)} passing the annotation instance and the package name of the class being
 * loaded. If the predicate returns {@code true}, the class is permitted regardless of annotation-based rules.
 * </p>
 *
 * <h2>BiPredicate Contract</h2>
 * <p>
 * The two parameters passed to {@link #test(Object, Object)} are:
 * </p>
 * <ul>
 *   <li><b>{@link ElementPackageRequest}</b> — the annotation instance, providing configuration context such as
 *       the package name patterns in {@link ElementPackageRequest#value()}</li>
 *   <li><b>String</b> — the name of the package containing the class being evaluated</li>
 * </ul>
 * <p>
 * Passing the annotation instance as the first argument allows a single {@code PackageRequest} implementation to
 * behave differently depending on how the annotation is configured at each call site, without needing a separate
 * implementation class per package.
 * </p>
 *
 * <h2>Difference from TypeRequest</h2>
 * <p>
 * {@link TypeRequest} matches against individual binary class names. {@code PackageRequest} matches against the
 * containing package name, permitting all classes within a matched package. Use {@code PackageRequest} when you
 * want to permit an entire namespace, and {@link TypeRequest} when you need to permit specific individual types.
 * </p>
 *
 * <h2>Usage</h2>
 * <p>
 * For simple cases, the built-in {@link Literal} implementation (the default) permits all classes in packages
 * whose names appear in {@link ElementPackageRequest#value()}:
 * </p>
 * <pre>{@code
 * @ElementPackageRequest(value = {"com.example.shared", "com.example.api"})
 * package com.example.myelement;
 * }</pre>
 * <p>
 * For pattern-based matching, use {@link Wildcard} or {@link Regex}:
 * </p>
 * <pre>{@code
 * @ElementPackageRequest(request = PackageRequest.Wildcard.class, value = {"com.example.*"})
 * package com.example.myelement;
 * }</pre>
 *
 * @see Literal
 * @see Regex
 * @see Wildcard
 * @see TypeRequest
 * @see PermittedPackages
 * @see PermittedTypesClassLoader
 * @see ElementPackageRequest
 */
public interface PackageRequest extends BiPredicate<ElementPackageRequest, String> {

    /**
     * The default {@link PackageRequest} implementation. Permits all classes in a package whose name exactly
     * matches any of the names listed in {@link ElementPackageRequest#value()}. This is the default value of
     * {@link ElementPackageRequest#request()}, so an Element that only needs to permit a fixed set of known
     * packages can do so purely through the annotation without writing any code:
     *
     * <pre>{@code
     * @ElementPackageRequest(value = {"com.example.shared", "com.example.api"})
     * package com.example.myelement;
     * }</pre>
     */
    class Literal implements PackageRequest {
        @Override
        public boolean test(final ElementPackageRequest elementPackageRequest, final String packageName) {
            return Stream.of(elementPackageRequest.value()).anyMatch(packageName::equals);
        }
    }

    /**
     * A {@link PackageRequest} implementation that treats each entry in {@link ElementPackageRequest#value()} as a
     * regular expression matched against the package name:
     *
     * <pre>{@code
     * @ElementPackageRequest(request = PackageRequest.Regex.class, value = {"com\\.example\\.shared(\\..*)?}"})
     * package com.example.myelement;
     * }</pre>
     *
     * <p>
     * Patterns are compiled on each invocation. If the same pattern set is evaluated at high frequency,
     * consider a custom implementation that pre-compiles and caches the {@link Pattern} objects.
     * </p>
     */
    class Regex implements PackageRequest {
        @Override
        public boolean test(final ElementPackageRequest elementPackageRequest, final String packageName) {
            return Stream.of(elementPackageRequest.value())
                    .map(Pattern::compile)
                    .anyMatch(p -> p.matcher(packageName).matches());
        }
    }

    /**
     * A {@link PackageRequest} implementation that treats each entry in {@link ElementPackageRequest#value()} as a
     * wildcard pattern where {@code *} matches any sequence of characters. Dots are matched literally, making
     * this natural for package hierarchy patterns:
     *
     * <pre>{@code
     * @ElementPackageRequest(request = PackageRequest.Wildcard.class, value = {"com.example.*"})
     * package com.example.myelement;
     * }</pre>
     *
     * <p>
     * Patterns are compiled on each invocation. If the same pattern set is evaluated at high frequency,
     * consider a custom implementation that pre-compiles and caches the resulting {@link Pattern} objects.
     * </p>
     */
    class Wildcard implements PackageRequest {
        @Override
        public boolean test(final ElementPackageRequest elementPackageRequest, final String packageName) {
            return Stream.of(elementPackageRequest.value())
                    .map(w -> Stream.of(w.split("\\*", -1))
                            .map(Pattern::quote)
                            .collect(Collectors.joining(".*"))
                    )
                    .map(Pattern::compile)
                    .anyMatch(p -> p.matcher(packageName).matches());
        }
    }

}