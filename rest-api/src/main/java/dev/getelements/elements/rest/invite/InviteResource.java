package dev.getelements.elements.rest.invite;

import dev.getelements.elements.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.InviteService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Api(value = "Invite",
        description = "Manages inviting functions like inviting via phone number list.",
        authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
@Path("invite")
public class InviteResource {

    private ValidationHelper validationHelper;

    private InviteService inviteService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Match normalized phone numbers with requested list",
            notes = "Both phones from request and from DB are normalized and compared.")
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
