package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.annotation.ElementLocal;

/**
 * Factory type for the {@link ElementScopedElementRegistry}. This holds single static instance which it always returns.
 * It is intended to be used within the SPI implementation and not directly.
 *
 * The {@link ElementImplementationClassLoader} copies and re-defines this class for itself such that there is separate static
 * instance per class-loader ensuring that an {@link Element} has it's own unique instance.
 */
@ElementLocal
public class ElementScopedElementRegistrySupplier implements ElementRegistrySupplier {

    private static ElementRegistry instance;

    @Override
    public ElementRegistry get() {
        return instance;
    }

    /**
     * Called by the SPI Interfaces to set the instance.
     *
     * @param instance the instance to set.
     */
    public static void setInstance(final ElementRegistry instance) {

        if (ElementScopedElementRegistrySupplier.instance != null) {
            throw new IllegalStateException("Instance already set: " + instance);
        }

        ElementScopedElementRegistrySupplier.instance = instance;

    }


}
