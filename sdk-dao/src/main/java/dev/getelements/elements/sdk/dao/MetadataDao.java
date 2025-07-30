package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.metadata.MetadataNotFoundException;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.user.User;

import java.util.Optional;

/**
 * This is the MetadataDao which is used to create, update, and retrieve metadata objects in the database.
 */
@ElementServiceExport
public interface MetadataDao {

    /**
     * Lists all {@link Metadata} instances with the specified pagination constraints.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link Metadata} instances
     */
    Pagination<Metadata> getMetadatas(int offset, int count, User.Level accessLevel);

    /**
     * Lists all {@link Metadata} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param search
     * @return a {@link Pagination} of {@link Metadata} instances
     */
    Pagination<Metadata> searchMetadatas(int offset, int count, String search, User.Level accessLevel);

    /**
     * Finds an active metadata object by the object id.
     *
     * @param metadataId the metadata object ID
     * @return an {@link Optional} possibly containing the {@link Metadata}
     */
    Optional<Metadata> findMetadata(String metadataId, User.Level accessLevel);

    /**
     * Fetches a specific {@link Metadata} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param metadataId the template ID
     * @return the {@link Metadata}, never null
     */
    default Metadata getMetadata(String metadataId, User.Level accessLevel) {
        return findMetadata(metadataId, accessLevel).orElseThrow(MetadataNotFoundException::new);
    }


    /**
     * Finds an active metadata object by the object id.
     *
     * @param metadataName the metadata object ID
     * @return an {@link Optional} possibly containing the {@link Metadata}
     */
    Optional<Metadata> findMetadataByName(String metadataName, User.Level accessLevel);

    /**
     * Fetches a specific {@link Metadata} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param metadataName the template ID
     * @return the {@link Metadata}, never null
     */
    default Metadata getMetadataByName(final String metadataName, User.Level accessLevel) {
        return findMetadataByName(metadataName, accessLevel).orElseThrow(MetadataNotFoundException::new);
    }

    /**
     * Creates a new metadata object.
     *
     * @param metadata
     * @return
     */
    Metadata createMetadata(Metadata metadata);

    /**
     * Creates a new metadata object.
     *
     * @param metadata
     * @return
     */
    Metadata updateMetadata(Metadata metadata);

    /**
     * Deletes the {@link Metadata} with the supplied metadata ID.
     *
     * @param metadataId the metadata ID.
     */
    void softDeleteMetadata(String metadataId);

}
