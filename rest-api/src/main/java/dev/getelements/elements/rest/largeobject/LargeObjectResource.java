package dev.getelements.elements.rest.largeobject;

import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.largeobject.CreateLargeObjectFromUrlRequest;
import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.LargeObjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

@Api(
        value = "LargeObjects",
        description = "Allows for the manipulation of LargeObject types.",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
        }
)
@Path("large_object")
public class LargeObjectResource {

    private LargeObjectService largeObjectService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a LargeObject")
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {
        return getLargeObjectService().createLargeObject(createLargeObjectRequest);
    }

    @POST
    @Path("from_url")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a LargeObject from provided URL")
    public LargeObject createLargeObjectFromUrl(final CreateLargeObjectFromUrlRequest createRequest) throws IOException {
        return getLargeObjectService().createLargeObjectFromUrl(createRequest);
    }

    @PUT
    @Path("{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a LargeObject")
    public LargeObject updateLargeObject(
            @PathParam("largeObjectId") final String largeObjectId,
            final UpdateLargeObjectRequest updateLargeObjectRequest) {
        return getLargeObjectService().updateLargeObject(largeObjectId, updateLargeObjectRequest);
    }

    @PUT
    @Path("{largeObjectId}/content")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a LargeObject content")
    public LargeObject updateLargeObjectContents(
            @PathParam("largeObjectId") final String largeObjectId,
            final InputStream inputStream) {
        try {
            return getLargeObjectService().updateLargeObject(largeObjectId, inputStream);
        } catch (IOException e) {
            throw new InternalException("Caught exception processing upload.");
        }
    }

    @GET
    @Path("{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a LargeObject")
    public LargeObject getLargeObject(@PathParam("largeObjectId") final String largeObjectId) {
        return getLargeObjectService().getLargeObject(largeObjectId);
    }

    @DELETE
    @Path("{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a LargeObject")
    public void deleteLargeObject(@PathParam("largeObjectId") final String objectId) throws IOException {
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
