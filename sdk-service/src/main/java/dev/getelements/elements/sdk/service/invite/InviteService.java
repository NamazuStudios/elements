package dev.getelements.elements.sdk.service.invite;

import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.invite.CreateSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.invite.RedeemSuperUserInviteRequest;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface InviteService {

    int PROFILES_PER_USER_LIMIT = 5;
    int USERS_PER_PHONE_LIMIT = 5;

    /** Superuser invite base URL for email links. */
    @ElementDefaultAttribute(value = "", description = "Public base URL for superuser invite links.")
    String SUPERUSER_INVITE_BASE_URL = "dev.getelements.elements.superuser.invite.base_url";

    /** Token validity in hours. */
    @ElementDefaultAttribute(value = "24", description = "Superuser invite token validity in hours.")
    String SUPERUSER_INVITE_EXPIRY_HOURS = "dev.getelements.elements.superuser.invite.expiry_hours";

    /**
     * Get an {@link InviteViaPhonesResponse} by invite request data with phone number list
     *
     * @param inviteRequest the request
     * @return the {@link InviteViaPhonesResponse}
     */
    InviteViaPhonesResponse inviteViaPhoneNumbers(InviteViaPhonesRequest inviteRequest, int offset, int count);

    /**
     * Creates a superuser invitation and sends an email to the invitee.
     * Only superusers may call this method.
     *
     * @param request the invitation request
     * @return the created invitation
     */
    SuperUserInvite createSuperUserInvite(CreateSuperUserInviteRequest request);

    /**
     * Lists all superuser invitations, newest first.
     * Only superusers may call this method.
     *
     * @param offset the offset
     * @param count the count
     * @return paginated invitations
     */
    Pagination<SuperUserInvite> listSuperUserInvites(int offset, int count);

    /**
     * Revokes a pending superuser invitation.
     * Only superusers may call this method.
     *
     * @param token the invitation token to revoke
     * @return the revoked invitation
     */
    SuperUserInvite revokeSuperUserInvite(String token);

    /**
     * Redeems a superuser invitation. The authenticated user's email must match the invited email.
     *
     * @param request the redemption request
     * @return the redeemed invitation
     */
    SuperUserInvite redeemSuperUserInvite(RedeemSuperUserInviteRequest request);

}
