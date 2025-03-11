package dev.getelements.elements.sdk.util.reflection;

import dev.getelements.elements.sdk.exception.SdkException;

import static java.lang.String.format;

/**
 * Some basic utilities for accessing reflections within SDK code.
 */
public class ElementReflectionUtils {

    private static final ElementReflectionUtils instance = new ElementReflectionUtils();

    /**
     * Gets the static shared instance.
     *
     * @return the {@link ElementReflectionUtils}
     */
    public static ElementReflectionUtils getInstance() {
        return instance;
    }

    /**
     * Gets the {@link Package} from the package-info class.
     *
     * @param name the package name
     * @return the {@link Package}
     */
    public Package getPackageForElementsAnnotations(final String name) {
        return getPackageForElementsAnnotations(name, getClass().getClassLoader());
    }

    /**
     * Gets the {@link Package} from the package-info class.
     *
     * @param name the package name
     * @param classLoader the {@link ClassLoader} to use
     * @return the {@link Package}
     */
    public Package getPackageForElementsAnnotations(final String name, final ClassLoader classLoader) {
        try {
            final var fqn = format("%s.package-info", name);
            return classLoader.loadClass(fqn).getPackage();
        } catch (ClassNotFoundException ex) {
            throw new SdkException(ex);
        }
    }

}
