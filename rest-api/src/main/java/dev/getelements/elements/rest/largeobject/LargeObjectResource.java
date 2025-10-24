package dev.getelements.elements.rest.largeobject;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectFromUrlRequest;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.sdk.service.largeobject.LargeObjectService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;

@Path("large_object")
public class LargeObjectResource {

    private LargeObjectService largeObjectService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates a LargeObject")
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {
        return getLargeObjectService().createLargeObject(createLargeObjectRequest);
    }

    @POST
    @Path("from_url")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates a LargeObject from provided URL")
    public LargeObject createLargeObjectFromUrl(final CreateLargeObjectFromUrlRequest createRequest) throws IOException {
        return getLargeObjectService().createLargeObjectFromUrl(createRequest);
    }

    @PUT
    @Path("{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates a LargeObject")
    public LargeObject updateLargeObject(
            @PathParam("largeObjectId") final String largeObjectId,
            final UpdateLargeObjectRequest updateLargeObjectRequest) {
        return getLargeObjectService().updateLargeObject(largeObjectId, updateLargeObjectRequest);
    }

    @PUT
    @Path("{largeObjectId}/content")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Updates a LargeObject content")
    public LargeObject updateLargeObjectContents(
            @PathParam("largeObjectId") final String largeObjectId,
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetails) {
        try {
            final var originalFilename = fileDetails.getFileName();

            return getLargeObjectService().updateLargeObject(largeObjectId, inputStream, originalFilename);
        } catch (IOException e) {
            throw new InternalException("Caught exception processing upload.");
        }
    }

    @GET
    @Path("{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a LargeObject")
    public LargeObject getLargeObject(@PathParam("largeObjectId") final String largeObjectId) {
        return getLargeObjectService().getLargeObject(largeObjectId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a LargeObject")
    public Pagination<LargeObject> getLargeObjects(
            @QueryParam("offset")  @DefaultValue("0")  final int offset,
            @QueryParam("count")   @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {
        return getLargeObjectService().getLargeObjects(offset, count, search);
    }

    @DELETE
    @Path("{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Deletes a LargeObject")
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
