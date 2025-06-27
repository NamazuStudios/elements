package dev.getelements.elements.sdk.record;

import java.util.List;

/**
 * Represents a collection of SPI implementations for Elements.
 * This record holds a list of {@link ElementSpiImplementationRecord} instances.
 */
public record ElementSpiImplementationsRecord(List<ElementSpiImplementationRecord> spis) {

    /**
     * Test if any supplied types are part of the Element SPI implementations.
     *
     * @param aClass the {@link Class} to check
     * @return true if any SPI implementation is part of the Element, false otherwise
     */
    public boolean isPathOfElement(final Class<?> aClass) {
        return spis()
                .stream()
                .anyMatch(spi -> spi.isPartOfElement(aClass));
    }

}
