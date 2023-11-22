package dev.getelements.elements.model.schema;

import java.util.List;

import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.ARRAY;

/**
 * An interface for a type which has metadata spec properties.
 */
public interface MetadataSpecPropertiesContainer {

    /**
     * Gets the type of this container.
     *
     * @return this container's type
     */
    MetadataSpecPropertyType getType();

    /**
     * Gets all {@link MetadataSpecProperty}.
     *
     * @return A {@link List<MetadataSpecProperty>}
     */
    List<MetadataSpecProperty> getProperties();

    /**
     * If this is an {@link MetadataSpecPropertyType#ARRAY} container, then this will fetch the
     * {@link MetadataSpecProperty} contained inside the array.
     *
     * @return the type
     */
    default MetadataSpecProperty getArrayTypeProperty() {

        final var properties = getProperties();

        if (!ARRAY.equals(getType())) {
            throw new IllegalStateException("Not of ARRAY type.");
        } else if (properties == null) {
            throw new IllegalStateException("'properties' array is null");
        } else if (properties.size() != 1) {
            throw new IllegalStateException("'properties' array must be a single element array.");
        }

        return properties.get(0);

    }

}
