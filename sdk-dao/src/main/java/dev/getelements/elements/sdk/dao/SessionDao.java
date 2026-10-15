package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Manages instances of {@link Session}.
 */
@ElementServiceExport
@ElementEventProducer(
        value = SessionDao.SESSION_CREATED,
        parameters = Session.class,
        description = "Called when a session was created."
)
@ElementEventProducer(
        value = SessionDao.SESSION_CREATED,
        parameters = {Session.class, Transaction.class},
        description = "Called when a session was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = SessionDao.SESSION_UPDATED,
        parameters = Session.class,
        description = "Called when a session was refreshed/updated."
)
@ElementEventProducer(
        value = SessionDao.SESSION_UPDATED,
        parameters = {Session.class, Transaction.class},
        description = "Called when a session was refreshed/updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = SessionDao.SESSION_DELETED,
        parameters = Session.class,
        description = "Called when a session was blacklisted/deleted."
)
@ElementEventProducer(
        value = SessionDao.SESSION_DELETED,
        parameters = {Session.class, Transaction.class},
        description = "Called when a session was blacklisted/deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface SessionDao {

    String SESSION_CREATED = "dev.getelements.elements.sdk.model.dao.session.created";

    String SESSION_UPDATED = "dev.getelements.elements.sdk.model.dao.session.updated";

    String SESSION_DELETED = "dev.getelements.elements.sdk.model.dao.session.deleted";

    /**
     * Gets the {@link Session} by it's session id, as returned by {@link SessionCreation#getSessionSecret()} ()}.
     * Throwing an appropriate exception type if the {@link Session} can't be found.
     *
     * @param sessionSecret the session's ID
     * @return the {@link Session} never null
     */
    Session getBySessionSecret(String sessionSecret);

    /**
     * Refreshs the {@link Session} with the supplied expiry.
     *
     * @param sessionSecret the session secret key
     * @param expiry        the expiry timestamp, as expression milliseconds since the Unix epoch
     * @return the updated {@link Session}
     */
    Session refresh(String sessionSecret, long expiry);

    /**
     * Creates a {@link Session} with the provided {@link Session} object.  This will return an instance of
     * {@link SessionCreation} providing a secret key which can be used to access the {@link Session} in the future.
     *
     * @param session
     * @return the {@link SessionCreation} as it was created in the database
     */
    SessionCreation create(Session session);

    /**
     * Deletes the {@link Session} instance.  The secret is determined by {@link SessionCreation#getSessionSecret()}.
     *
     * @param sessionSecret the session secret
     */
    void blacklist(String sessionSecret);

    /**
     * Gets the {@link Session} by its server-side session id. This is only valid for sessions issued
     * with a signed session token, whose {@code sid} claim identifies the server-side record. The
     * caller is responsible for having verified the token's signature prior to calling this method.
     * Throwing an appropriate exception type if the {@link Session} can't be found or has expired.
     *
     * @param sessionId the server-side session id
     * @return the {@link Session} never null
     */
    Session getSessionBySessionId(String sessionId);

    /**
     * Deletes the {@link Session} with the supplied server-side session id. This is only valid for
     * sessions issued with a signed session token, whose {@code sid} claim identifies the server-side
     * record. The caller is responsible for having verified the token's signature prior to calling
     * this method.
     *
     * @param sessionId the server-side session id
     */
    void deleteSessionBySessionId(String sessionId);

    /**
     * Deletes all {@link Session} instances belonging to the supplied user. Used to explicitly revoke
     * every outstanding session, e.g. when a password is reset or a user is deactivated.
     *
     * @param userId the user's id
     */
    void deleteSessionsForUser(String userId);

}
