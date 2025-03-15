package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementPackage;

/**
 * A record for the {@link ElementPackage}
 */
public record ElementPackageRecord(String name, boolean recursive) {

    /**
     * Constructs a record from the supplied {@link ElementPackage} annotation.
     *
     * @param elementPackage the annotation
     * @return a new instance
     */
    public static ElementPackageRecord from(final ElementPackage elementPackage) {

        final var value = elementPackage.value().toLowerCase();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Element package name cannot be empty");
        }

        return new ElementPackageRecord(value, elementPackage.recursive());

    }

}
