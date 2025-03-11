package dev.getelements.elements.rest.savedata;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.savedata.CreateSaveDataDocumentRequest;
import dev.getelements.elements.sdk.model.savedata.SaveDataDocument;
import dev.getelements.elements.sdk.model.savedata.UpdateSaveDataDocumentRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.savedata.SaveDataDocumentService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Path("save_data")
public class SaveDataDocumentResource {

    private ValidationHelper validationHelper;

    private SaveDataDocumentService saveDataDocumentService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Search Save Data Documents",
        description = "Gets all save data documents available to the user.")
    public Pagination<SaveDataDocument> getSaveDataDocuments(
            @QueryParam("offset") @DefaultValue("0")
            final int offset,
            @QueryParam("count")  @DefaultValue("20")
            final int count,
            @QueryParam("userId") @DefaultValue("")
            final String userId,
            @QueryParam("profileId") @DefaultValue("")
            final String profileId,
            @QueryParam("search") @DefaultValue("")
            final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getSaveDataDocumentService().getSaveDataDocuments(offset, count, userId, profileId) :
            getSaveDataDocumentService().getSaveDataDocuments(offset, count, query);

    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get save data document.",
        description = "Gets a single save data document")
    public SaveDataDocument getSaveDataDocument(
            @PathParam("id")
            final String nameOrId) {
        return getSaveDataDocumentService().getSaveDataDocument(nameOrId);
    }

    @GET
    @Path("user/{userId}/{slot}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get save data document.",
            description = "Gets a single save data document based on UserID and slot. This is a convenience method which" +
                    "allows the client to fetch a save data based on slot an user id.")
    public SaveDataDocument getUserSaveDataDocumentBySlot(
            @PathParam("userId")
            final String userId,
            @PathParam("slot")
            final int slot) {
        return getSaveDataDocumentService().getUserSaveDataDocumentBySlot(userId, slot);
    }

    @GET
    @Path("profile/{profileId}/{slot}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get save data document.",
            description = "Gets a single save data document based on Profile ID and slot. This is a convenience method which" +
                    "allows the client to fetch a save data based on slot an profile id.")
    public SaveDataDocument getProfileSaveDataDocumentBySlot(
            @PathParam("profileId")
            final String profileId,
            @PathParam("slot")
            final int slot) {
        return getSaveDataDocumentService().getProfileSaveDataDocumentBySlot(profileId, slot);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Creates a save data document.",
        description = "Gets a single save data document."
    )
    public SaveDataDocument createSaveDocument(
            final CreateSaveDataDocumentRequest createSaveDataDocumentRequest) {
        getValidationHelper().validateModel(createSaveDataDocumentRequest);
        return getSaveDataDocumentService().createSaveDataDocument(createSaveDataDocumentRequest);
    }

    @PUT
    @Path("{saveDataDocumentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a save data document.",
            description = "Gets a single save data document."
    )
    public SaveDataDocument updateSaveDocument(
            @PathParam("saveDataDocumentId")
            final String saveDataDocumentId,
            final UpdateSaveDataDocumentRequest updateSaveDataDocumentRequest) {
        getValidationHelper().validateModel(updateSaveDataDocumentRequest);
        return getSaveDataDocumentService().updateSaveDataDocument(saveDataDocumentId, updateSaveDataDocumentRequest);
    }

    @DELETE
    @Path("{saveDataDocumentId}")
    @Operation(summary = "Deletes a save data document")
    public void deleteSaveDocument(
            @PathParam("saveDataDocumentId")
            final String saveDataDocumentId) {
        getSaveDataDocumentService().deleteSaveDocument(saveDataDocumentId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public SaveDataDocumentService getSaveDataDocumentService() {
        return saveDataDocumentService;
    }

    @Inject
    public void setSaveDataDocumentService(SaveDataDocumentService saveDataDocumentService) {
        this.saveDataDocumentService = saveDataDocumentService;
    }

}
