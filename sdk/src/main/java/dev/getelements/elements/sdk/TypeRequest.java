package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementTypeRequest;

import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * An Element-declared predicate that requests access to specific types not otherwise permitted by the system-side
 * {@link PermittedTypes} or {@link PermittedPackages} visibility rules. Where {@link PermittedTypes} is registered
 * by the host system via {@link java.util.ServiceLoader} and governs what the system chooses to expose,
 * {@code TypeRequest} is declared by the Element itself, allowing it to assert that it needs access to a particular
 * type at load time.
 * </p>
 *
 * <h2>How It Works</h2>
 * <p>
 * Implementations are referenced from the {@link dev.getelements.elements.sdk.annotation.ElementTypeRequest}
 * annotation, which is placed on a package declaration ({@code package-info.java}) within the Element. When the
 * {@link PermittedTypesClassLoader} evaluates a class for visibility, it discovers any {@code TypeRequest}
 * implementations declared by the Element and invokes {@link #test(Object, Object)} passing the annotation
 * instance and the binary class name. If the predicate returns {@code true}, the class is permitted regardless
 * of annotation-based rules.
 * </p>
 *
 * <h2>BiPredicate Contract</h2>
 * <p>
 * The two parameters passed to {@link #test(Object, Object)} are:
 * </p>
 * <ul>
 *   <li><b>{@link dev.getelements.elements.sdk.annotation.ElementTypeRequest}</b> — the annotation instance,
 *       providing configuration context such as the explicit type name list in
 *       {@link dev.getelements.elements.sdk.annotation.ElementTypeRequest#value()}</li>
 *   <li><b>String</b> — the binary name of the class as passed to {@code ClassLoader.loadClass()}</li>
 * </ul>
 * <p>
 * Passing the annotation instance as the first argument allows a single {@code TypeRequest} implementation to
 * behave differently depending on how the annotation is configured at each call site, without needing a separate
 * implementation class per package.
 * </p>
 *
 * <h2>Difference from PermittedTypes</h2>
 * <p>
 * {@link PermittedTypes} is a system-side contract: the host registers implementations to declare what it is
 * willing to expose. {@code TypeRequest} is an Element-side contract: the Element declares what it needs.
 * This separation of concerns allows the host to control the default visibility surface while still giving
 * Elements a way to opt in to additional types they depend on.
 * </p>
 *
 * <h2>Usage</h2>
 * <p>
 * For simple cases, the built-in {@link Literal} implementation (the default) permits types whose binary names
 * appear in {@link dev.getelements.elements.sdk.annotation.ElementTypeRequest#value()}:
 * </p>
 * <pre>{@code
 * @ElementTypeRequest(value = {"com.example.shared.Foo", "com.example.shared.Bar"})
 * package com.example.myelement;
 * }</pre>
 * <p>
 * For more complex matching, supply a custom implementation via
 * {@link dev.getelements.elements.sdk.annotation.ElementTypeRequest#request()}:
 * </p>
 * <pre>{@code
 * @ElementTypeRequest(request = MyTypeRequest.class)
 * package com.example.myelement;
 *
 * public class MyTypeRequest implements TypeRequest {
 *     {@literal @}Override
 *     public boolean test(ElementTypeRequest annotation, String binaryName) {
 *         return binaryName.startsWith("com.example.shared.");
 *     }
 * }
 * }</pre>
 *
 * @see Literal
 * @see PermittedTypes
 * @see PermittedPackages
 * @see PermittedTypesClassLoader
 * @see dev.getelements.elements.sdk.annotation.ElementTypeRequest
 */
public interface TypeRequest extends BiPredicate<ElementTypeRequest, String> {

    /**
     * The default {@link TypeRequest} implementation. Permits a class whose binary name exactly matches any of the
     * names listed in {@link ElementTypeRequest#value()}. This is the default value of
     * {@link ElementTypeRequest#request()}, so an Element that only needs to permit a fixed set of known types
     * can do so purely through the annotation without writing any code:
     *
     * <pre>{@code
     * @ElementTypeRequest(value = {"com.example.shared.Foo", "com.example.shared.Bar"})
     * package com.example.myelement;
     * }</pre>
     */
    class Literal implements TypeRequest {
        @Override
        public boolean test(final ElementTypeRequest elementTypeRequest, final String s) {
            return Stream.of(elementTypeRequest.value()).anyMatch(s::equals);
        }
    }

    /**
     * A {@link TypeRequest} implementation that treats each entry in {@link ElementTypeRequest#value()} as a
     * regular expression and permits a class whose binary name matches any of them:
     *
     * <pre>{@code
     * @ElementTypeRequest(request = TypeRequest.Regex.class, value = {"com\\.example\\.shared\\..*"})
     * package com.example.myelement;
     * }</pre>
     *
     * <p>
     * Patterns are compiled on each invocation. If the same pattern set is evaluated at high frequency,
     * consider a custom implementation that pre-compiles and caches the {@link Pattern} objects.
     * </p>
     */
    class Regex implements TypeRequest {
        @Override
        public boolean test(final ElementTypeRequest elementTypeRequest, final String s) {
            return Stream.of(elementTypeRequest.value())
                    .map(Pattern::compile)
                    .anyMatch(p -> p.matcher(s).matches());
        }
    }

    /**
     * A {@link TypeRequest} implementation that treats each entry in {@link ElementTypeRequest#value()} as a
     * wildcard pattern where {@code *} matches any sequence of characters. Dots and other characters are matched
     * literally, making this a natural fit for package-prefix style patterns:
     *
     * <pre>{@code
     * @ElementTypeRequest(request = TypeRequest.Wildcard.class, value = {"com.example.shared.*"})
     * package com.example.myelement;
     * }</pre>
     *
     * <p>
     * Patterns are compiled on each invocation. If the same pattern set is evaluated at high frequency,
     * consider a custom implementation that pre-compiles and caches the resulting {@link Pattern} objects.
     * </p>
     */
    class Wildcard implements TypeRequest {
        @Override
        public boolean test(final ElementTypeRequest elementTypeRequest, final String s) {
            return Stream.of(elementTypeRequest.value())
                    .map(w -> Stream.of(w.split("\\*", -1))
                            .map(Pattern::quote)
                            .collect(Collectors.joining(".*"))
                    )
                    .map(Pattern::compile)
                    .anyMatch(p -> p.matcher(s).matches());
        }
    }

}
