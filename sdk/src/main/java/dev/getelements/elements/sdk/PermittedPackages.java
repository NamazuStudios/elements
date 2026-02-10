package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.annotation.ElementPublic;

import java.util.function.Predicate;

/**
 * <p>
 * A predicate-based package visibility rule that determines which packages (and all their classes) should be
 * accessible through a {@link PermittedTypesClassLoader}. Implementations of this interface are discovered via
 * {@link java.util.ServiceLoader} and are used to programmatically control package-level visibility in plugin
 * or Element isolation scenarios.
 * </p>
 *
 * <h2>How It Works</h2>
 * <p>
 * When {@link PermittedTypesClassLoader} loads a class from its delegate classloader, it evaluates the class's
 * package against all discovered {@code PermittedPackages} implementations. If any implementation's
 * {@link PermittedPackages#test(Object)} method returns {@code true}, <b>all classes in that package</b> are permitted and can
 * be loaded. This provides a convenient way to expose entire packages without having to permit individual types.
 * </p>
 *
 * <h2>Interaction with PermittedTypesClassLoader</h2>
 * <p>
 * {@link PermittedTypesClassLoader} discovers implementations of this interface using {@link java.util.ServiceLoader}
 * from the delegate classloader. Multiple implementations can coexist, and if <i>any</i> implementation permits
 * a package, all classes in that package become accessible. This allows for composable visibility rules where
 * different modules or libraries can contribute their own permitted packages.
 * </p>
 *
 * <h2>Difference from PermittedTypes</h2>
 * <p>
 * While {@link PermittedTypes} operates at the individual class level, {@code PermittedPackages} operates at the
 * package level. If a package is permitted, all classes within it are accessible (subject to other visibility rules).
 * Use {@code PermittedPackages} when you want to expose entire APIs or modules, and {@link PermittedTypes} when
 * you need fine-grained control over specific types.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class MyPermittedPackages implements PermittedPackages {
 *     @Override
 *     public boolean test(Package pkg) {
 *         // Permit all packages starting with "com.mycompany.api"
 *         return pkg.getName().startsWith("com.mycompany.api");
 *     }
 * }
 * }</pre>
 *
 * <p>
 * Register the implementation via {@code META-INF/services/dev.getelements.elements.sdk.PermittedPackages}.
 * </p>
 *
 * @see PermittedTypesClassLoader
 * @see PermittedTypes
 * @see ElementPublic
 * @see ElementPrivate
 */
public interface PermittedPackages extends Predicate<Package> {

    /**
     * Gets the description of the permitted packages. This is intended to return a meaningful description of what
     * and why the permitted packages are shared with the underlying isolated Element.
     *
     * @return the description
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }

}
