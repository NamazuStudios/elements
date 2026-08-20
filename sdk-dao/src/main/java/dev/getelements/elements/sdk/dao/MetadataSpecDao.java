package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.schema.MetadataSpecNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

/**
 * Created by garrettmcspadden on 11/23/21.
 */

@ElementServiceExport
@ElementEventProducer(
        value = MetadataSpecDao.METADATA_SPEC_CREATED,
        parameters = MetadataSpec.class,
        description = "Called when a metadata spec was created."
)
@ElementEventProducer(
        value = MetadataSpecDao.METADATA_SPEC_CREATED,
        parameters = {MetadataSpec.class, Transaction.class},
        description = "Called when a metadata spec was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = MetadataSpecDao.METADATA_SPEC_UPDATED,
        parameters = MetadataSpec.class,
        description = "Called when a metadata spec was updated."
)
@ElementEventProducer(
        value = MetadataSpecDao.METADATA_SPEC_UPDATED,
        parameters = {MetadataSpec.class, Transaction.class},
        description = "Called when a metadata spec was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = MetadataSpecDao.METADATA_SPEC_DELETED,
        parameters = MetadataSpec.class,
        description = "Called when a metadata spec was deleted."
)
@ElementEventProducer(
        value = MetadataSpecDao.METADATA_SPEC_DELETED,
        parameters = {MetadataSpec.class, Transaction.class},
        description = "Called when a metadata spec was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface MetadataSpecDao {

    String METADATA_SPEC_CREATED = "dev.getelements.elements.sdk.model.dao.metadata.spec.created";

    String METADATA_SPEC_UPDATED = "dev.getelements.elements.sdk.model.dao.metadata.spec.updated";

    String METADATA_SPEC_DELETED = "dev.getelements.elements.sdk.model.dao.metadata.spec.deleted";

    /**
     * Lists all {@link MetadataSpec} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link MetadataSpec} instances
     */
    Pagination<MetadataSpec> getActiveMetadataSpecs(int offset, int count);

    /**
     * Finds an active metadata spec by the spec id.
     *
     * @param metadataSpecId the metadata spec ID
     * @return an {@link Optional} possibly containing the {@link MetadataSpec}
     */
    Optional<MetadataSpec> findActiveMetadataSpec(String metadataSpecId);

    /**
     * Fetches a specific {@link MetadataSpec} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param metadataSpecId the template ID
     * @return the {@link MetadataSpec}, never null
     */
    default MetadataSpec getActiveMetadataSpec(String metadataSpecId) {
        return findActiveMetadataSpec(metadataSpecId).orElseThrow(MetadataSpecNotFoundException::new);
    }


    /**
     * Finds an active metadata spec by the spec id.
     *
     * @param metadataSpecName the metadata spec ID
     * @return an {@link Optional} possibly containing the {@link MetadataSpec}
     */
    Optional<MetadataSpec> findActiveMetadataSpecByName(String metadataSpecName);

    /**
     * Fetches a specific {@link MetadataSpec} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param metadataSpecName the template ID
     * @return the {@link MetadataSpec}, never null
     */
    default MetadataSpec getActiveMetadataSpecByName(final String metadataSpecName) {
        return findActiveMetadataSpecByName(metadataSpecName).orElseThrow(MetadataSpecNotFoundException::new);
    }

    /**
     * Creates a new metadata spec.
     *
     * @param metadataSpec
     * @return
     */
    MetadataSpec createMetadataSpec(MetadataSpec metadataSpec);

    /**
     * Creates a new metadata spec.
     *
     * @param metadataSpec
     * @return
     */
    MetadataSpec updateActiveMetadataSpec(MetadataSpec metadataSpec);

    /**
     * Deletes the {@link MetadataSpec} with the supplied metadataSpec ID.
     *
     * @param metadataSpecId the metadataSpec ID.
     */
    void deleteMetadataSpec(String metadataSpecId);

}
