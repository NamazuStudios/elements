package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Element;
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

    /**
     * Checks if the supplied {@link Package} is part of the {@link Element} attached to this record.
     *
     * @param aPackage a {@link Package}
     * @return true if part of this {@link Element}, false otherwise
     */
    public boolean isPartOfElement(final Package aPackage) {
        return recursive()
                ? aPackage.getName().startsWith(name())
                : aPackage.getName().equals(name);
    }

}
