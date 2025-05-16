package dev.getelements.elements.rest.schema;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.sdk.model.schema.EditorSchema;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.sdk.model.schema.json.JsonSchema;
import dev.getelements.elements.sdk.service.schema.MetadataSpecService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Created by tuantran on 04/12/22.
 */
@Path("/metadata_spec")
public class MetadataSpecResource {

    private MetadataSpecService metadataSpecService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get Metadata Specs",
            description = "Gets a pagination of Metadata Specs for the given query.")
    public Pagination<MetadataSpec> getMetadataSpecs(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {
        return getMetadataSpecService().getMetadataSpecs(offset, count);
    }

    @GET
    @Path("{metadataSpecNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific Metadata Spec",
            description = "Gets a specific MetadataSpec by name or Id.")
    public MetadataSpec getMetadataSpec(@PathParam("metadataSpecNameOrId") String metadataSpecNameOrId) {
        return getMetadataSpecService().getMetadataSpec(metadataSpecNameOrId);
    }

    @GET
    @Path("{metadataSpecName}/schema.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific JSON Schema",
            description = "Gets a specific JSON Schema backed by the supplied MetadataSpec by name."
    )
    public JsonSchema getJsonSchema(@PathParam("metadataSpecName") String metadataSpecName) {
        return getMetadataSpecService().getJsonSchema(metadataSpecName);
    }

    @GET
    @Path("{metadataSpecName}/editor.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific Metadata Editor Schema",
            description = "Gets a specific Metadata Editor Schema backed by the supplied MetadataSpec by name."
    )
    public EditorSchema getEditorSchema(@PathParam("metadataSpecName") String metadataSpecName) {
        return getMetadataSpecService().getEditorSchema(metadataSpecName);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Creates a new Metadata Spec definition",
            description = "Creates a new Metadata Spec definition.")
    public MetadataSpec createMetadataSpec(final CreateMetadataSpecRequest tokenRequest) {
        return getMetadataSpecService().createMetadataSpec(tokenRequest);
    }

    @PUT
    @Path("{metadataSpecId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Updates a Metadata Spec",
            description = "Updates a MetadataSpec with the specified id.")
    public MetadataSpec updateMetadataSpec(@PathParam("metadataSpecId") String metadataSpecId, final UpdateMetadataSpecRequest updateMetadataSpecRequest) {
        return getMetadataSpecService().updateMetadataSpec(metadataSpecId, updateMetadataSpecRequest);
    }

    @DELETE
    @Path("{metadataSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Deletes a MetadataSpec",
            description = "Deletes a MetadataSpec with the specified id.")
    public void deleteMetadataSpec(@PathParam("metadataSpecId") String metadataSpecId) {

        metadataSpecId = Strings.nullToEmpty(metadataSpecId).trim();

        if (metadataSpecId.isEmpty()) {
            throw new NotFoundException();
        }

        getMetadataSpecService().deleteMetadataSpec(metadataSpecId);
    }

    public MetadataSpecService getMetadataSpecService() {
        return metadataSpecService;
    }

    @Inject
    public void setMetadataSpecService(MetadataSpecService metadataSpecService) {
        this.metadataSpecService = metadataSpecService;
    }

}
