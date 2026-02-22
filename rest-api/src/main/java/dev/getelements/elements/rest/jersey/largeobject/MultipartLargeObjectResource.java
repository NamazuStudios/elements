package dev.getelements.elements.rest.jersey.largeobject;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.SubjectRequest;
import dev.getelements.elements.sdk.service.largeobject.LargeObjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;

@Path("large_object_mp")
public class MultipartLargeObjectResource {

    private LargeObjectService largeObjectService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation( summary = "Creates a LargeObject with content")
    public LargeObject createMultipartLargeObject(
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

    public LargeObjectService getLargeObjectService() {
        return largeObjectService;
    }

    @Inject
    public void setLargeObjectService(LargeObjectService largeObjectService) {
        this.largeObjectService = largeObjectService;
    }

}
