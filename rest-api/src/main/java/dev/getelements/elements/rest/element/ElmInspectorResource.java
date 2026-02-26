package dev.getelements.elements.rest.element;

import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.system.ElementPathRecordMetadata;
import dev.getelements.elements.sdk.service.system.ElementInspectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Schema(description = "Provides metadata as to the contents of an Element distribution (ie ELM file).")
@Path("elm/inspector")
public class ElmInspectorResource {

    private ElementInspectorService elementInspectorService;

    @GET
    @Operation(summary = "Inspects the artifact with the supplied coordinates.")
    @Path("artifact/{coordinates}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ElementPathRecordMetadata> getElementPathRecordForArtifact(
            @PathParam("coordinates")
            @Schema(description = "The coordinates of the ELM artifact.")
            final String coordinates) {
        return getElementInspectorService().inspectElementArtifact(coordinates);
    }

    @POST
    @Path("upload")
    @Operation(summary = "Inspects an ELM file uploaded via multipart form.")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ElementPathRecordMetadata> inspectUploadedElm(
            @FormDataParam("elm")
            @Schema(description = "The ELM file to inspect.")
            final InputStream inputStream) {
        try {
            return getElementInspectorService().inspectElement(inputStream);
        } catch (IOException ex) {
            throw new InternalException("Caught exception processing ELM upload.", ex);
        }
    }

    @GET
    @Operation(summary = "Inspects the artifact with the supplied large object id.")
    @Path("large_object/{largeObjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ElementPathRecordMetadata> getElementPathRecordForLargeObjectId(
            @PathParam("largeObjectId")
            @Schema(description = "The the large object ID of the ELM.")
            final String largeObjectId
    ) {
        return getElementInspectorService().inspectElementLargeObject(largeObjectId);
    }

    public ElementInspectorService getElementInspectorService() {
        return elementInspectorService;
    }

    @Inject
    public void setElementInspectorService(ElementInspectorService elementInspectorService) {
        this.elementInspectorService = elementInspectorService;
    }

}
