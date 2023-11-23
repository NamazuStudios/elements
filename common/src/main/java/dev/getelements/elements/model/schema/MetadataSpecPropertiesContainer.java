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

}
