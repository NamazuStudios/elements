package dev.getelements.elements.rest.largeobject;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.service.largeobject.LargeObjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "LargeObjects",
        description = "Allows for the manipulation of LargeObject types.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("lo")
public class LargeObjectResource {

    private LargeObjectService largeObjectService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a LargeObject")
    public LargeObject createLargeObject(final CreateLargeObjectRequest objectRequest) {
        return getLargeObjectService().createLargeObject(objectRequest);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a LargeObject")
    public LargeObject updateLargeObject(final UpdateLargeObjectRequest objectRequest) {
        return getLargeObjectService().updateLargeObject(objectRequest);
    }

    @GET
    @Path("{objectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a LargeObject")
    public LargeObject getLargeObject(@PathParam("objectId") final String objectId) {
        return getLargeObjectService().getLargeObject(objectId);
    }

    //TODO: clarify delete logic. Persist delete or flag as removed/deactivated
    @DELETE
    @Path("{objectId}")
    @ApiOperation(value = "Deletes a LargeObject")
    public void deleteLargeObject(@PathParam("objectId") final String objectId) {
        getLargeObjectService().deleteLargeObject(objectId);
    }

    public LargeObjectService getLargeObjectService() {
        return largeObjectService;
    }

    @Inject
    public void setLargeObjectService(LargeObjectService largeObjectService) {
        this.largeObjectService = largeObjectService;
    }
}
