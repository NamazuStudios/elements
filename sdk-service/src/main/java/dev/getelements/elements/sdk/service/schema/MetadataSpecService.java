package dev.getelements.elements.sdk.service.schema;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.sdk.model.schema.EditorSchema;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.sdk.model.schema.json.JsonSchema;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages instances of {@link MetadataSpec}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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

    /**
     * Gets the JSON Schema for the metadata spec name.
     *
     * @param metadataSpecName the name of the metadata spec
     * @return the metadata spec schema
     */
    JsonSchema getJsonSchema(String metadataSpecName);

    EditorSchema getEditorSchema(String metadataSpecName);

}
