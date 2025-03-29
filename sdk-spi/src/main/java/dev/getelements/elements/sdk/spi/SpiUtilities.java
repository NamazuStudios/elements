package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.exception.SdkException;

import java.lang.reflect.InvocationTargetException;

/**
 * Some utility classes useful to SPI Implementations.
 */
public class SpiUtilities {

    private SpiUtilities() {}

    private static final SpiUtilities instance = new SpiUtilities();

    public static SpiUtilities getInstance() {
        return instance;
    }

    /**
     * Binds the supplied object to the provider type.
     *
     * @param classLoader the {@link ClassLoader} to use
     * @param providerType the SPI provider type which has a static setInstance method
     * @param instance the instance to set
     */
    public <T> void bind(
            final ClassLoader classLoader,
            final Class<?> providerType,
            final T instance,
            final Class<? super T> interfaceT) {
        try {
            final var supplierCls = classLoader.loadClass(providerType.getName());
            final var setInstance = supplierCls.getMethod("setInstance", interfaceT);
            setInstance.invoke(null, instance);
        } catch (ClassNotFoundException |
                 NoSuchMethodException |
                 InvocationTargetException |
                 IllegalAccessException ex) {
            throw new SdkException(ex);
        }
    }

}
