package com.namazustudios.socialengine.rest.savedata;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;
import com.namazustudios.socialengine.service.SaveDataDocumentService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "Save Data",
        description = "Save Data Documents allow the client to save private data for the application on a per user " +
                      "or per-profile or per-profile basis. The contents of a save data are up to the application. " +
                      "The API will always pass the data through as a string with no validation. The size of the " +
                      "document is limited to around 4MiB as per database limitations. It is not recommended to store " +
                      "large amounts of data in this API.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
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
        notes = "Gets a single save data document.")
    public SaveDataDocument getSaveDataDocument(
            @PathParam("id")
            final String nameOrId) {
        return getSaveDataDocumentService().getSaveDataDocument(nameOrId);
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
