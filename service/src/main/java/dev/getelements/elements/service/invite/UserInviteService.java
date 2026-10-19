package dev.getelements.elements.service.invite;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.invite.CreateSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.RedeemSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.service.invite.InviteService;

/**
 * User-level {@link InviteService}. Phone-based matching is available to any authenticated user.
 * Issuing, listing, and revoking superuser invitations are superuser-only and are rejected here.
 * Redemption is permitted: elevating an account is the entire point of the flow, so the invited
 * account is, by definition, not yet a superuser when it redeems.
 */
public class UserInviteService extends AbstractSuperUserInviteService {

    @Override
    public SuperUserInvite createSuperUserInvite(CreateSuperUserInviteRequest request) {
        throw new ForbiddenException("Superuser privileges are required to issue invitations.");
    }

    @Override
    public Pagination<SuperUserInvite> listSuperUserInvites(int offset, int count) {
        throw new ForbiddenException("Superuser privileges are required to list invitations.");
    }

    @Override
    public SuperUserInvite revokeSuperUserInvite(String token) {
        throw new ForbiddenException("Superuser privileges are required to revoke invitations.");
    }

    @Override
    public SuperUserInvite redeemSuperUserInvite(RedeemSuperUserInviteRequest request) {
        return doRedeemSuperUserInvite(request);
    }

}
