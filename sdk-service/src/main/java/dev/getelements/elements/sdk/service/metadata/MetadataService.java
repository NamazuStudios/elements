package dev.getelements.elements.sdk.service.metadata;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.metadata.*;

public interface MetadataService {

    /**
     * Gets a metadata with unique metadata id.
     *
     * @param metadataId the Metadata id
     *
     * @return the metadata id
     */
    Metadata getMetadataObject(final String metadataId);

    /**
     * Gets a list of metadata objects.
     *
     * @param offset the offset
     * @param count the count
     * @return the PaginatedEntry of metadata objects
     */
    Pagination<Metadata> getMetadataObjects(int offset, int count);

    /**
     * Gets a list of metadata objects.
     *
     * @param offset the offset
     * @param count the count
     * @param search the search query
     *
     * @return the PaginatedEntry of metadata objects
     */
    Pagination<Metadata> getMetadataObjects(int offset, int count, String search);

    /**
     * Creates a new metadata.  The service may override or reject the request based on the current metadata access level.
     *
     * @param createMetadataRequest the metadata to create
     * @return the Metadata, as it was created by the database
     */
    Metadata createMetadata(CreateMetadataRequest createMetadataRequest);

    /**
     * Updates a metadata, preserving the metadata's password.
     *
     * @param metadataId  the metadata ID to update
     * @param updateMetadataRequest the metadata to update
     * @return the Metadata, as it was updated
     */
    Metadata updateMetadata(String metadataId, UpdateMetadataRequest updateMetadataRequest);

    /**
     * Soft deletes a metadata from the system by wiping its fields clean but retaining the db id to prevent broken refs.
     *
     * @param metadataId the metadataId
     */
    void softDeleteMetadata(final String metadataId);
}
