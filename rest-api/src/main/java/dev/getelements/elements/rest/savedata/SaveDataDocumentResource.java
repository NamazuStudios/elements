package dev.getelements.elements.rest.savedata;

import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.savedata.CreateSaveDataDocumentRequest;
import dev.getelements.elements.model.savedata.SaveDataDocument;
import dev.getelements.elements.model.savedata.UpdateSaveDataDocumentRequest;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.SaveDataDocumentService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Api(value = "Save Data",
        description = "Save Data Documents allow the client to save private data for the application on a per user " +
                      "or per-profile or per-profile basis. The contents of a save data are up to the application. " +
                      "The API will always pass the data through as a string with no validation. The size of the " +
                      "document is limited to around 4MiB as per database limitations. It is not recommended to store " +
                      "large amounts of data in this API.",
        authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
@Path("save_data")
public class SaveDataDocumentResource {

    private ValidationHelper validationHelper;

    private SaveDataDocumentService saveDataDocumentService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Search Save Data Documents",
        notes = "Gets all save data documents available to the user.")
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
    @ApiOperation(
        value = "Get save data document.",
        notes = "Gets a single save data document")
    public SaveDataDocument getSaveDataDocument(
            @PathParam("id")
            final String nameOrId) {
        return getSaveDataDocumentService().getSaveDataDocument(nameOrId);
    }

    @GET
    @Path("user/{userId}/{slot}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get save data document.",
            notes = "Gets a single save data document based on UserID and slot. This is a convenience method which" +
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
    @ApiOperation(
            value = "Get save data document.",
            notes = "Gets a single save data document based on Profile ID and slot. This is a convenience method which" +
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
    @ApiOperation(
        value = "Creates a save data document.",
        notes = "Gets a single save data document."
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
    @ApiOperation(
            value = "Creates a save data document.",
            notes = "Gets a single save data document."
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
    @ApiOperation(
            value = "Deletes a save data document",
            notes = ""
    )
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
