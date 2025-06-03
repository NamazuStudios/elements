package dev.getelements.elements.rest.invite;

import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.invite.InviteService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("invite")
public class InviteResource {

    private ValidationHelper validationHelper;

    private InviteService inviteService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Match normalized phone numbers with requested list",
            description = "Both phones from request and from DB are normalized and compared.")
    public InviteViaPhonesResponse getMatchedUserProfilesWithPhoneNumbers(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            final InviteViaPhonesRequest inviteRequest) {
        getValidationHelper().validateModel(inviteRequest);
        return getInviteService().inviteViaPhoneNumbers(inviteRequest, offset, count);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public InviteService getInviteService() {
        return inviteService;
    }

    @Inject
    public void setInviteService(InviteService inviteService) {
        this.inviteService = inviteService;
    }

}
