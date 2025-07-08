package dev.getelements.elements.sdk.util.reflection;

import dev.getelements.elements.sdk.ElementStandardBeanProperties;
import dev.getelements.elements.sdk.exception.SdkException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

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
