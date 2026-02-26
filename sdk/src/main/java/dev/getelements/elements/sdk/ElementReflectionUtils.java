package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Some basic utilities for accessing reflections within SDK code.
 */
public class ElementReflectionUtils implements ElementStandardBeanProperties {

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
     * Gets the package-info {@link Class} for the supplied package name.
     *
     * @param name the package name
     * @return the {@link Package}
     */
    public Class<?> getPackageInfo(final String name) {
        return getPackageInfo(name, getClass().getClassLoader());
    }

    /**
     * Gets the corresponding package-info {@link Class} from the supplied {@link Class}.
     *
     * @param cls the class
     * @return the package-info {@link Class}
     */
    public Class<?> getPackageInfo(final Class<?> cls) {
        return getPackageInfo(cls.getPackageName(), cls.getClassLoader());
    }

    /**
     * Gets the package-info {@link Class} for the supplied package name.
     *
     * @param name the package name
     * @param classLoader the {@link ClassLoader} to use
     * @return the {@link Package}
     */
    public Class<?> getPackageInfo(final String name, final ClassLoader classLoader) {
        try {
            final var fqn = format("%s.package-info", name);
            return classLoader.loadClass(fqn);
        } catch (ClassNotFoundException ex) {
            throw new SdkException(ex);
        }
    }

    /**
     * Finds the package-info {@link Class} for the supplied package name, returning empty if none exists.
     *
     * @param name the package name
     * @return an {@link Optional} containing the package-info {@link Class}, or empty if not found
     */
    public Optional<Class<?>> findPackageInfo(final String name) {
        return findPackageInfo(name, getClass().getClassLoader());
    }

    /**
     * Finds the corresponding package-info {@link Class} from the supplied {@link Class},
     * returning empty if none exists.
     *
     * @param cls the class
     * @return an {@link Optional} containing the package-info {@link Class}, or empty if not found
     */
    public Optional<Class<?>> findPackageInfo(final Class<?> cls) {
        return findPackageInfo(cls.getPackageName(), cls.getClassLoader());
    }

    /**
     * Finds the package-info {@link Class} for the supplied package name using the given classloader,
     * returning empty if none exists.
     *
     * @param name the package name
     * @param classLoader the {@link ClassLoader} to use
     * @return an {@link Optional} containing the package-info {@link Class}, or empty if not found
     */
    public Optional<Class<?>> findPackageInfo(final String name, final ClassLoader classLoader) {

        if (classLoader == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(classLoader.loadClass(format("%s.package-info", name)));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }

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
        return getPackageInfo(name, classLoader).getPackage();
    }

    /**
     * Injects the standard bean properties into the supplied object. Standard bean property names are defined in
     * {@link ElementStandardBeanProperties}.
     *
     * @param target the target
     * @param objects the beans
     * @return the target object
     * @param <T> the target type
     */
    public <T> T injectBeanProperties(final T target, final Object ... objects) {

        try {

            final var info = Introspector.getBeanInfo(target.getClass());

            for (var descriptor : info.getPropertyDescriptors()) {
                for (var bean : objects) {

                    if (setIfMatch(target, descriptor, ELEMENT_RECORD_PROPERTY, bean))
                        break;

                    if (setIfMatch(target, descriptor, ELEMENT_DEFINITION_RECORD_PROPERTY, bean))
                        break;

                    if (setIfMatch(target, descriptor, SERVICE_LOCATOR_PROPERTY, bean))
                        break;

                    if (setIfMatch(target, descriptor, ELEMENT_REGISTRY_PROPERTY, bean))
                        break;

                }
            }

        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ex) {
            throw new SdkException(ex);
        }

        return target;

    }

    private boolean setIfMatch(
            final Object target,
            final PropertyDescriptor descriptor,
            final StandardBeanProperty<?> property,
            final Object value) throws InvocationTargetException, IllegalAccessException {

        if (!descriptor.getName().equals(property.name()))
            return false;

        final var beanClass = value.getClass();

        if (!property.type().isAssignableFrom(beanClass))
            return false;

        descriptor.getWriteMethod().invoke(target, value);
        return true;

    }

}
