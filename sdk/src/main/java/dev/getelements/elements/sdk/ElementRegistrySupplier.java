package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkCallbackException;

import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Gets a shared instance of {@link ElementRegistry} which can be used to load {@link Element} instances into the
 * application. From within a {@link ElementType#ISOLATED_CLASSPATH} instance, this will be shared and isolated from
 * the others.
 *
 */
public interface ElementRegistrySupplier extends Supplier<ElementRegistry> {

    /**
     * Gets the default shared {@link ElementRegistry} for the current {@link Element}.
     *
     * @return the {@link ElementRegistrySupplier}
     */
    static ElementRegistrySupplier getElementLocal(final Class<?> aClass) {
        return getElementLocal(aClass.getClassLoader());
    }

    /**
     * Gets the default shared {@link ElementRegistrySupplier} for the current {@link Element}.
     *
     * @return the {@link ElementRegistry}
     */
    static ElementRegistrySupplier getElementLocal(final ClassLoader classLoader) {
        return ServiceLoader
                .load(ElementRegistrySupplier.class, classLoader)
                .stream()
                .findFirst()
                .orElseThrow(SdkCallbackException::new)
                .get();
    }

}
