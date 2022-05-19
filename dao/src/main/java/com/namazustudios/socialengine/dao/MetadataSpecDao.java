package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.model.schema.template.CreateMetadataSpecRequest;
import com.namazustudios.socialengine.model.schema.template.MetadataSpec;
import com.namazustudios.socialengine.model.schema.template.UpdateMetadataSpecRequest;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by garrettmcspadden on 11/23/21.
 */
@Expose({
        @ExposedModuleDefinition("namazu.elements.dao.metadata.spec"),
        @ExposedModuleDefinition(
                value = "namazu.socialengine.dao.metadata.spec",
                deprecated = @DeprecationDefinition("Use namazu.elements.dao.token.template instead"))
})
public interface MetadataSpecDao {

    /**
     * Lists all {@link MetadataSpec} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link NeoToken} instances
     */
    Pagination<MetadataSpec> getMetadataSpecs(int offset, int count);

    /**
     * Fetches a specific {@link MetadataSpec} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param metadataSpecId the template ID
     * @return the {@link MetadataSpec}, never null
     */
    MetadataSpec getMetadataSpec(String metadataSpecId);

    /**
     * Updates the supplied {@link MetadataSpec}.
     *
     * @param metadataSpecId the id of the token to update
     * @param updateMetadataSpecRequest the update request for the metaDataSpec.
     * @return the {@link MetadataSpec} as it was changed by the service.
     */
    MetadataSpec updateMetadataSpec(String metadataSpecId, UpdateMetadataSpecRequest updateMetadataSpecRequest);

    /**
     * Creates a new metadata spec.
     *
     * @param createMetadataSpecRequest the {@link CreateMetadataSpecRequest} with the information to create
     * @return the {@link MetadataSpec} as it was created by the service.
     */
    MetadataSpec createMetadataSpec(CreateMetadataSpecRequest createMetadataSpecRequest);

    /**
     * Creates a new template by cloning an existing {@link MetadataSpec} definition.
     *
     * @param metadataSpec the {@link MetadataSpec} with the information to clone
     * @return the {@link MetadataSpec} as it was created by the service.
     */
    MetadataSpec cloneMetadataSpec(MetadataSpec metadataSpec);

    /**
     * Deletes the {@link MetadataSpec} with the supplied metadataSpec ID.
     *
     * @param metadataSpecId the metadataSpec ID.
     */
    void deleteMetadataSpec(String metadataSpecId);
}
