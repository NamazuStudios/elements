package dev.getelements.elements.rest.jersey.largeobject;

import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.SubjectRequest;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.LargeObjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

@Api(
        value = "MultipartLargeObjects",
        description = "Allows for the manipulation of LargeObject types.",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
        }
)
@Path("large_object_mp")
public class MultipartLargeObjectResource {

    private LargeObjectService largeObjectService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Creates a LargeObject")
    public LargeObject createLargeObject(
            @FormDataParam("object") final FormDataBodyPart objectBodyPart,
            @FormDataParam("metadata") final CreateLargeObjectRequest createLargeObjectRequest) {

        if (createLargeObjectRequest.getMimeType() == null) {
            createLargeObjectRequest.setMimeType(objectBodyPart.getName());
        }

        if (createLargeObjectRequest.getRead() == null) {
            createLargeObjectRequest.setRead(SubjectRequest.newDefaultRequest());
        }

        if (createLargeObjectRequest.getWrite() == null) {
            createLargeObjectRequest.setWrite(SubjectRequest.newDefaultRequest());
        }

        if (createLargeObjectRequest.getDelete() == null) {
            createLargeObjectRequest.setDelete(SubjectRequest.newDefaultRequest());
        }

        try {
            final var stream = objectBodyPart.getValueAs(InputStream.class);
            return getLargeObjectService().createLargeObject(createLargeObjectRequest, stream);
        } catch (IOException ex) {
            throw new InternalException("Caught exception processing upload.");
        }

    }

    @PUT
    @Path("{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Creates a LargeObject")
    public LargeObject updateLargeObject(
            @PathParam("largeObjectId") final String largeObjectId,
            @FormDataParam("object") final FormDataBodyPart objectBodyPart,
            @FormDataParam("metadata") final UpdateLargeObjectRequest updateLargeObjectRequest) {

        if (updateLargeObjectRequest.getMimeType() == null) {
            updateLargeObjectRequest.setMimeType(objectBodyPart.getName());
        }

        if (updateLargeObjectRequest.getRead() == null) {
            updateLargeObjectRequest.setRead(SubjectRequest.newDefaultRequest());
        }

        if (updateLargeObjectRequest.getWrite() == null) {
            updateLargeObjectRequest.setWrite(SubjectRequest.newDefaultRequest());
        }

        if (updateLargeObjectRequest.getDelete() == null) {
            updateLargeObjectRequest.setDelete(SubjectRequest.newDefaultRequest());
        }

        try {
            final var stream = objectBodyPart.getValueAs(InputStream.class);
            return getLargeObjectService().updateLargeObject(largeObjectId, updateLargeObjectRequest, stream);
        } catch (IOException ex) {
            throw new InternalException("Caught exception processing upload.");
        }

    }

    public LargeObjectService getLargeObjectService() {
        return largeObjectService;
    }

    @Inject
    public void setLargeObjectService(LargeObjectService largeObjectService) {
        this.largeObjectService = largeObjectService;
    }

}
