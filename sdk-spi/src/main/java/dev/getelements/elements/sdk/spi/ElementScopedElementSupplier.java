package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementSupplier;
import dev.getelements.elements.sdk.exception.SdkElementNotFoundException;
import dev.getelements.elements.sdk.annotation.ElementLocal;

/**
 * Maintains a singleton instance of an {@link Element}.
 */
@ElementLocal
public class ElementScopedElementSupplier implements ElementSupplier {

    private static Element instance;

    @Override
    public Element get() {

        if (instance == null) {
            throw new SdkElementNotFoundException("No current Element.");
        }

        return instance;

    }

    /**
     * Called by the SPI Interfaces to set the instance.
     *
     * @param instance the instance to set.
     */
    public static void setInstance(final Element instance) {

        if (ElementScopedElementSupplier.instance != null) {
            throw new IllegalStateException("Instance already set: " + instance.getElementRecord());
        }

        ElementScopedElementSupplier.instance = instance;

    }

}
