package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;

/**
 * For types which have a {@link MetadataSpec}. For internal use only.
 */
@ElementPrivate
public interface Indexable {

    /**
     * Plans the indexes. Writing the contents of the to-be indexed content to the database.
     */
    void plan();

    /**
     * Builds the indexes based on the last plan operation.
     */
    void buildIndexes();

}
