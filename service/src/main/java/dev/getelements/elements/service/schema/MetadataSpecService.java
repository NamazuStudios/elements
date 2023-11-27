package dev.getelements.elements.service.schema;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.Unscoped;

/**
 * Manages instances of {@link MetadataSpec}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.metadata.spec"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.unscoped.metadata.spec",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.metadata.spec",
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.metadata.spec instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.metadata.spec",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.unscoped.metadata.spec instead.")
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
