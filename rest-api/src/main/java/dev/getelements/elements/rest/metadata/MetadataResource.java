package dev.getelements.elements.rest.metadata;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.metadata.CreateMetadataRequest;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.metadata.UpdateMetadataRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.metadata.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("metadata")
public class MetadataResource {

    private MetadataService metadataService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Metadata",
            description = "Searches all metadata in the system and returning all matches against " +
                    "the given search filter.")
    public Pagination<Metadata> getMetadataObjects(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {
        return metadataService.getMetadataObjects(offset, count, search);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific Metadata object",
            description = "Gets a specific metadata object by name or id.")
    public Metadata getMetadataObject(@PathParam("id") final String id) {
        return metadataService.getMetadataObject(id);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Metadata object",
            description = "Creates a new Metadata object with the provided details.")
    public Metadata createMetadata(final CreateMetadataRequest createMetadataRequest) {

        validationHelper.validateModel(createMetadataRequest);

        return metadataService.createMetadata(createMetadataRequest);
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a specific Metadata object",
            description = "Updates a specific metadata object by name or id.")
    public Metadata updateMetadata(
            @PathParam("id") final String id,
            final UpdateMetadataRequest updateMetadataRequest) {

        validationHelper.validateModel(updateMetadataRequest);

        return metadataService.updateMetadata(id, updateMetadataRequest);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a specific Metadata object",
            description = "Deletes a specific metadata object by name or id.")
    public void deleteMetadata(@PathParam("id") final String id) {
        metadataService.softDeleteMetadata(id);
    }

    public MetadataService getMetadataService() {
        return metadataService;
    }

    @Inject
    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
