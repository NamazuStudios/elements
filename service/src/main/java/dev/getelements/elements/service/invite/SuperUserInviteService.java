package dev.getelements.elements.service.invite;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.invite.CreateSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.RedeemSuperUserInviteRequest;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.service.invite.InviteService;

/**
 * Superuser-level {@link InviteService}. All operations are permitted; invitation issuance records
 * the current superuser as the issuing administrator.
 */
public class SuperUserInviteService extends AbstractSuperUserInviteService {

    @Override
    public SuperUserInvite createSuperUserInvite(CreateSuperUserInviteRequest request) {
        return doCreateSuperUserInvite(request);
    }

    @Override
    public Pagination<SuperUserInvite> listSuperUserInvites(int offset, int count) {
        return doListSuperUserInvites(offset, count);
    }

    @Override
    public SuperUserInvite revokeSuperUserInvite(String token) {
        return doRevokeSuperUserInvite(token);
    }

    @Override
    public SuperUserInvite redeemSuperUserInvite(RedeemSuperUserInviteRequest request) {
        return doRedeemSuperUserInvite(request);
    }

}
