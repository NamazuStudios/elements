package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.annotation.ElementPublic;

import java.util.function.Predicate;

/**
 * <p>
 * A predicate-based type visibility rule that determines which classes should be accessible through a
 * {@link PermittedTypesClassLoader}. Implementations of this interface are discovered via {@link java.util.ServiceLoader}
 * and are used to programmatically control class visibility in plugin or Element isolation scenarios.
 * </p>
 *
 * <h2>How It Works</h2>
 * <p>
 * When {@link PermittedTypesClassLoader} loads a class from its delegate classloader, it evaluates the class
 * against all discovered {@code PermittedTypes} implementations. If any implementation's {@link #test(Object)}
 * method returns {@code true}, the class is permitted and can be loaded. This provides a programmatic way to
 * define type visibility rules alongside annotation-based controls like {@link ElementPublic} and {@link ElementPrivate}.
 * </p>
 *
 * <h2>Interaction with PermittedTypesClassLoader</h2>
 * <p>
 * {@link PermittedTypesClassLoader} discovers implementations of this interface using {@link java.util.ServiceLoader}
 * from the delegate classloader. Multiple implementations can coexist, and if <i>any</i> implementation permits
 * a type, that type becomes accessible. This allows for composable visibility rules where different modules or
 * libraries can contribute their own permitted types.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class MyPermittedTypes implements PermittedTypes {
 *     @Override
 *     public boolean test(Class<?> clazz) {
 *         // Permit all classes from java.util package
 *         return clazz.getPackageName().startsWith("java.util");
 *     }
 * }
 * }</pre>
 *
 * <p>
 * Register the implementation via {@code META-INF/services/dev.getelements.elements.sdk.PermittedTypes}.
 * </p>
 *
 * @see PermittedTypesClassLoader
 * @see PermittedPackages
 * @see ElementPublic
 * @see ElementPrivate
 */
public interface PermittedTypes extends Predicate<Class<?>> {

    /**
     * Gets the description of the permitted types. This is intended to return a meaningful description of what
     * and why the permitted types are shared with the underlying isolated Element.
     *
     * @return the description
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }

}
