package com.namazustudios.socialengine.rest.schema;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.schema.template.CreateMetadataSpecRequest;
import com.namazustudios.socialengine.model.schema.template.MetadataSpec;
import com.namazustudios.socialengine.model.schema.template.UpdateMetadataSpecRequest;
import com.namazustudios.socialengine.service.schema.MetadataSpecService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by tuantran on 04/12/22.
 */
@Api(value = "Metadata Specs",
        description = "Allows for the storage and retrieval of Metadata Specs.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("/blockchain/metadata_spec")
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
    public MetadataSpec getToken(@PathParam("metadataSpecNameOrId") String metadataSpecNameOrId) {
        return getMetadataSpecService().getMetadataSpec(metadataSpecNameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Metadata Spec definition",
            notes = "Creates a new Metadata Spec definition.")
    public MetadataSpec createToken(final CreateMetadataSpecRequest tokenRequest) {
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
