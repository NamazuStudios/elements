package dev.getelements.elements.rest.invite;

import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.invite.InviteService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import dev.getelements.elements.sdk.model.invite.CreateSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.RedeemSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.Pagination;

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

    @POST
    @Path("superuser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a superuser invitation", description = "Creates a superuser invitation and sends an email to the invitee. Requires superuser access.")
    public SuperUserInvite createSuperUserInvite(final CreateSuperUserInviteRequest request) {
        getValidationHelper().validateModel(request);
        return getInviteService().createSuperUserInvite(request);
    }

    @GET
    @Path("superuser")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List superuser invitations", description = "Lists superuser invitations. Requires superuser access.")
    public Pagination<SuperUserInvite> listSuperUserInvites(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count") @DefaultValue("20") final int count) {
        return getInviteService().listSuperUserInvites(offset, count);
    }

    @DELETE
    @Path("superuser/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Revoke a superuser invitation", description = "Revokes a pending superuser invitation. Requires superuser access.")
    public SuperUserInvite revokeSuperUserInvite(@PathParam("token") final String token) {
        return getInviteService().revokeSuperUserInvite(token);
    }

    @POST
    @Path("superuser/redeem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Redeem a superuser invitation", description = "Redeems a superuser invitation. Requires authentication with matching email.")
    public SuperUserInvite redeemSuperUserInvite(final RedeemSuperUserInviteRequest request) {
        getValidationHelper().validateModel(request);
        return getInviteService().redeemSuperUserInvite(request);
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
