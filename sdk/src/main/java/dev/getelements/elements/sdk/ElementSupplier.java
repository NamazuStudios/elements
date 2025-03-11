package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkException;

import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * When running within an {@link Element}, returns the current {@link Element} instance.
 */
public interface ElementSupplier extends Supplier<Element> {

    /**
     * Gets the current {@link ElementSupplier}, using the {@link ClassLoader} from the supplied
     * {@link Class#getClassLoader()} call. This is useful for within inside Element code.
     *
     * Note, this is only for use within {@link ElementType#ISOLATED_CLASSPATH} instances.
     *
     * @param aClass the {@link Class<?>} to use to locate the {@link ClassLoader}
     *
     * @return the {@link ElementSupplier}
     */
    static ElementSupplier getElementLocal(final Class<?> aClass) {
        return getElementLocal(aClass.getClassLoader());
    }

    /**
     * Gets the current {@link ElementSupplier} using the supplied {@link ClassLoader}.
     *
     * Note, this is only for use within {@link ElementType#ISOLATED_CLASSPATH} instances.
     *
     * @param classLoader the {@link Class<?>} to use to locate the {@link ClassLoader}
     *
     * @return the {@link ElementSupplier}
     */
    static ElementSupplier getElementLocal(final ClassLoader classLoader) {
        return ServiceLoader
                .load(ElementSupplier.class, classLoader)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No SPI for " + ElementSupplier.class.getName()))
                .get();
    }

}
