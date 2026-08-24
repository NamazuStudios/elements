package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.*;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;
import java.util.Optional;

@ElementServiceExport
@ElementEventProducer(
        value = AuthSchemeDao.AUTH_SCHEME_CREATED,
        parameters = AuthScheme.class,
        description = "Called when an auth scheme was created."
)
@ElementEventProducer(
        value = AuthSchemeDao.AUTH_SCHEME_CREATED,
        parameters = {AuthScheme.class, Transaction.class},
        description = "Called when an auth scheme was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = AuthSchemeDao.AUTH_SCHEME_UPDATED,
        parameters = AuthScheme.class,
        description = "Called when an auth scheme was updated."
)
@ElementEventProducer(
        value = AuthSchemeDao.AUTH_SCHEME_UPDATED,
        parameters = {AuthScheme.class, Transaction.class},
        description = "Called when an auth scheme was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = AuthSchemeDao.AUTH_SCHEME_DELETED,
        parameters = AuthScheme.class,
        description = "Called when an auth scheme was deleted."
)
@ElementEventProducer(
        value = AuthSchemeDao.AUTH_SCHEME_DELETED,
        parameters = {AuthScheme.class, Transaction.class},
        description = "Called when an auth scheme was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface AuthSchemeDao {

    String AUTH_SCHEME_CREATED = "dev.getelements.elements.sdk.model.dao.auth.scheme.created";

    String AUTH_SCHEME_UPDATED = "dev.getelements.elements.sdk.model.dao.auth.scheme.updated";

    String AUTH_SCHEME_DELETED = "dev.getelements.elements.sdk.model.dao.auth.scheme.deleted";

    /**
     * Lists all {@link AuthScheme} instances
     *
     * @param offset
     * @param count
     * @param tags
     * @return a {@link Pagination} of {@link AuthScheme} instances
     */
    Pagination<AuthScheme> getAuthSchemes(int offset, int count, List<String> tags);

    /**
     * Finds an {@link AuthScheme}, returning an {@link Optional<AuthScheme>}.
     *
     * @param authSchemeId the auth scheme id
     * @return an {@link Optional<AuthScheme>}
     */
    Optional<AuthScheme> findAuthScheme(String authSchemeId);

    /**
     * Fetches a specific {@link AuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link AuthScheme}, never null
     */
    default AuthScheme getAuthScheme(final String authSchemeId) {
        return findAuthScheme(authSchemeId).orElseThrow(AuthSchemeNotFoundException::new);
    }

    /**
     * Gets all {@link AuthScheme} instances with the supplied audiences.
     *
     * @param audience the audience
     */
    List<AuthScheme> getAuthSchemesByAudience(final List<String> audience);

    /**
     * Updates the supplied {@link AuthScheme}
     *
     * @param authScheme the {@link UpdateAuthSchemeRequest} with the information to update the authScheme
     * @return a {@link UpdateAuthSchemeResponse} as it was created
     */
    AuthScheme updateAuthScheme(AuthScheme authScheme);

    /**
     * Creates an {@link AuthScheme}
     *
     * @param authScheme the {@link CreateAuthSchemeRequest} with the information to create the authScheme
     * @return a {@link CreateAuthSchemeResponse} as it was created
     */
    AuthScheme createAuthScheme(AuthScheme authScheme);

    /**
     * Deletes the {@link AuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}
