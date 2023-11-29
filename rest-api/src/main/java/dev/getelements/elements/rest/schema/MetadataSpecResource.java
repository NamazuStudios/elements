package dev.getelements.elements.rest.schema;

import com.google.common.base.Strings;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.model.schema.EditorSchema;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.model.schema.json.JsonSchema;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.schema.MetadataSpecService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by tuantran on 04/12/22.
 */
@Api(value = "Metadata Specs",
        description = "Allows for the storage and retrieval of Metadata Specs.",
        authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
@Path("/metadata_spec")
public class MetadataSpecResource {

    private MetadataSpecService metadataSpecService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Metadata Specs",
            notes = "Gets a pagination of Metadata Specs for the given query.")
    public Pagination<MetadataSpec> getMetadataSpecs(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {
        return getMetadataSpecService().getMetadataSpecs(offset, count);
    }

    @GET
    @Path("{metadataSpecNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Metadata Spec",
            notes = "Gets a specific MetadataSpec by name or Id.")
    public MetadataSpec getMetadataSpec(@PathParam("metadataSpecNameOrId") String metadataSpecNameOrId) {
        return getMetadataSpecService().getMetadataSpec(metadataSpecNameOrId);
    }

    @GET
    @Path("{metadataSpecName}/schema.json")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a specific JSON Schema",
            notes = "Gets a specific JSON Schema backed by the supplied MetadataSpec by name."
    )
    public JsonSchema getJsonSchema(@PathParam("metadataSpecName") String metadataSpecName) {
        return getMetadataSpecService().getJsonSchema(metadataSpecName);
    }

    @GET
    @Path("{metadataSpecName}/editor.json")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a specific Metadata Editor Schema",
            notes = "Gets a specific Metadata Editor Schema backed by the supplied MetadataSpec by name."
    )
    public EditorSchema getEditorSchema(@PathParam("metadataSpecName") String metadataSpecName) {
        return getMetadataSpecService().getEditorSchema(metadataSpecName);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Metadata Spec definition",
            notes = "Creates a new Metadata Spec definition.")
    public MetadataSpec createMetadataSpec(final CreateMetadataSpecRequest tokenRequest) {
        return getMetadataSpecService().createMetadataSpec(tokenRequest);
    }

    @PUT
    @Path("{metadataSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Metadata Spec",
            notes = "Updates a MetadataSpec with the specified id.")
    public MetadataSpec updateMetadataSpec(@PathParam("metadataSpecId") String metadataSpecId, final UpdateMetadataSpecRequest updateMetadataSpecRequest) {
        return getMetadataSpecService().updateMetadataSpec(metadataSpecId, updateMetadataSpecRequest);
    }

    @DELETE
    @Path("{metadataSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a MetadataSpec",
            notes = "Deletes a MetadataSpec with the specified id.")
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
