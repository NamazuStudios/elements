package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.user.User;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO for single-use superuser invitation tokens. An invitation binds an email address to a bearer
 * token which, when redeemed by an authenticated account controlling that email address, elevates
 * the account to {@link dev.getelements.elements.sdk.model.user.User.Level#SUPERUSER}.
 */
@ElementServiceExport
@ElementEventProducer(
        value = SuperUserInviteTokenDao.SUPERUSER_INVITE_CREATED,
        parameters = SuperUserInvite.class,
        description = "Called when a superuser invite was created.")
@ElementEventProducer(
        value = SuperUserInviteTokenDao.SUPERUSER_INVITE_CREATED,
        parameters = {SuperUserInvite.class, Transaction.class},
        description = "Called when a superuser invite was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction.")
@ElementEventProducer(
        value = SuperUserInviteTokenDao.SUPERUSER_INVITE_REDEEMED,
        parameters = SuperUserInvite.class,
        description = "Called when a superuser invite was redeemed.")
@ElementEventProducer(
        value = SuperUserInviteTokenDao.SUPERUSER_INVITE_REDEEMED,
        parameters = {SuperUserInvite.class, Transaction.class},
        description = "Called when a superuser invite was redeemed. This variant includes the transaction so that reactions to this event can be performed in the same transaction.")
@ElementEventProducer(
        value = SuperUserInviteTokenDao.SUPERUSER_INVITE_REVOKED,
        parameters = SuperUserInvite.class,
        description = "Called when a superuser invite was revoked.")
@ElementEventProducer(
        value = SuperUserInviteTokenDao.SUPERUSER_INVITE_REVOKED,
        parameters = {SuperUserInvite.class, Transaction.class},
        description = "Called when a superuser invite was revoked. This variant includes the transaction so that reactions to this event can be performed in the same transaction.")
public interface SuperUserInviteTokenDao {

    String SUPERUSER_INVITE_CREATED = "dev.getelements.elements.sdk.dao.superuser.invite.created";

    String SUPERUSER_INVITE_REDEEMED = "dev.getelements.elements.sdk.dao.superuser.invite.redeemed";

    String SUPERUSER_INVITE_REVOKED = "dev.getelements.elements.sdk.dao.superuser.invite.revoked";

    /**
     * Creates and persists a new superuser invitation.
     *
     * @param invitedBy    the superuser issuing the invitation
     * @param invitedEmail the invited email address
     * @param expiry       the expiry timestamp after which the invitation is invalid
     * @param createdAt    the creation timestamp
     * @return the opaque token string (also the database primary key)
     */
    String createInvite(User invitedBy, String invitedEmail, Timestamp expiry, Timestamp createdAt);

    /**
     * Returns the invitation if present, pending, and not expired.
     *
     * @param token the opaque token string
     * @return an {@link Optional} containing the invitation, or empty if absent, expired, redeemed, or revoked
     */
    Optional<SuperUserInvite> findToken(String token);

    /**
     * Returns the most recent pending, unexpired invitation for the given email address, if any.
     *
     * @param email the invited email address
     * @return an {@link Optional} containing the invitation, or empty if none is pending
     */
    Optional<SuperUserInvite> findPendingInviteByEmail(String email);

    /**
     * Lists all invitations, newest first.
     *
     * @param offset the pagination offset
     * @param count  the page size
     * @return a {@link Pagination} of invitations
     */
    Pagination<SuperUserInvite> getInvites(int offset, int count);

    /**
     * Marks a pending invitation redeemed. Fails (returns false) if the token is absent, expired,
     * redeemed, or revoked.
     *
     * @param token      the opaque token string
     * @param redeemedAt the redemption timestamp
     * @return true if the invitation was redeemed, false otherwise
     */
    boolean redeemToken(String token, Timestamp redeemedAt);

    /**
     * Marks a pending invitation revoked. Fails (returns false) if the token is absent, expired,
     * redeemed, or revoked.
     *
     * @param token     the opaque token string
     * @param revokedAt the revocation timestamp
     * @return true if the invitation was revoked, false otherwise
     */
    boolean revokeToken(String token, Timestamp revokedAt);

    /**
     * Returns the invitation regardless of state (including redeemed/revoked/expired), for
     * superuser-facing listing after a state transition.
     *
     * @param token the opaque token string
     * @return an {@link Optional} containing the invitation record, or empty if absent
     */
    Optional<SuperUserInvite> findTokenAnyState(String token);

}
