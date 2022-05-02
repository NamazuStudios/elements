package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.template.CreateMetadataSpecRequest;
import com.namazustudios.socialengine.model.blockchain.template.MetadataSpec;
import com.namazustudios.socialengine.model.blockchain.template.UpdateMetadataSpecRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

/**
 * Manages instances of {@link MetadataSpec}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.blockchain.metadata.spec"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.metadata.spec",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface MetadataSpecService {

    /**
     * Lists all {@link MetadataSpec} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @return a {@link Pagination} of {@link MetadataSpec} instances
     */
    Pagination<MetadataSpec> getMetadataSpecs(int offset, int count);

    /**
     * Fetches a specific {@link MetadataSpec} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param metadataSpecIdOrName the profile ID
     * @return the {@link MetadataSpec}, never null
     */
    MetadataSpec getMetadataSpec(String metadataSpecIdOrName);

    /**
     * Updates the supplied {@link MetadataSpec}.
     *
     * @param metadataSpecId the id of the metadata spec to update
     * @param metadataSpecRequest the token information to update
     * @return the {@link MetadataSpec} as it was changed by the service.
     */
    MetadataSpec updateMetadataSpec(String metadataSpecId, UpdateMetadataSpecRequest metadataSpecRequest);

    /**
     * Creates a new token using a pre-created template.
     *
     * @param metadataSpecRequest the {@link CreateMetadataSpecRequest} with the information to create
     * @return the {@link MetadataSpec} as it was created by the service.
     */
    MetadataSpec createMetadataSpec(CreateMetadataSpecRequest metadataSpecRequest);

    /**
     * Deletes the {@link MetadataSpec} with the supplied metadata Spec ID.
     *
     * @param metadataSpecId the metadata Spec ID.
     */
    void deleteMetadataSpec(String metadataSpecId);

}
