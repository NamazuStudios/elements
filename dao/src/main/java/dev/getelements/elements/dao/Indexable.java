package dev.getelements.elements.dao;

import dev.getelements.elements.model.schema.MetadataSpec;

/**
 * For types which have a {@link MetadataSpec}
 */
public interface Indexable {

    void plan();

    void buildIndexes();

}
