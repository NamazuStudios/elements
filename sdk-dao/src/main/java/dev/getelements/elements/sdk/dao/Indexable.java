package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * For types which have a {@link MetadataSpec}
 */
@ElementServiceExport
public interface Indexable {

    void plan();

    void buildIndexes();

}
