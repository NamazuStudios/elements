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
        value = OAuth2AuthSchemeDao.OAUTH2_AUTH_SCHEME_CREATED,
        parameters = OAuth2AuthScheme.class,
        description = "Called when an OAuth2 auth scheme was created."
)
@ElementEventProducer(
        value = OAuth2AuthSchemeDao.OAUTH2_AUTH_SCHEME_CREATED,
        parameters = {OAuth2AuthScheme.class, Transaction.class},
        description = "Called when an OAuth2 auth scheme was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = OAuth2AuthSchemeDao.OAUTH2_AUTH_SCHEME_UPDATED,
        parameters = OAuth2AuthScheme.class,
        description = "Called when an OAuth2 auth scheme was updated."
)
@ElementEventProducer(
        value = OAuth2AuthSchemeDao.OAUTH2_AUTH_SCHEME_UPDATED,
        parameters = {OAuth2AuthScheme.class, Transaction.class},
        description = "Called when an OAuth2 auth scheme was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = OAuth2AuthSchemeDao.OAUTH2_AUTH_SCHEME_DELETED,
        parameters = OAuth2AuthScheme.class,
        description = "Called when an OAuth2 auth scheme was deleted."
)
@ElementEventProducer(
        value = OAuth2AuthSchemeDao.OAUTH2_AUTH_SCHEME_DELETED,
        parameters = {OAuth2AuthScheme.class, Transaction.class},
        description = "Called when an OAuth2 auth scheme was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface OAuth2AuthSchemeDao {

    String OAUTH2_AUTH_SCHEME_CREATED = "dev.getelements.elements.sdk.model.dao.oauth2.auth.scheme.created";

    String OAUTH2_AUTH_SCHEME_UPDATED = "dev.getelements.elements.sdk.model.dao.oauth2.auth.scheme.updated";

    String OAUTH2_AUTH_SCHEME_DELETED = "dev.getelements.elements.sdk.model.dao.oauth2.auth.scheme.deleted";

    /**
     * Lists all {@link OAuth2AuthScheme} instances
     *
     * @param offset
     * @param count
     * @param tags
     * @return a {@link Pagination} of {@link OAuth2AuthScheme} instances
     */
    Pagination<OAuth2AuthScheme> getAuthSchemes(int offset, int count, List<String> tags);

    /**
     * Finds an {@link OAuth2AuthScheme}, returning an {@link Optional}.
     *
     * @param authSchemeNameOrId the auth scheme id
     * @return an {@link Optional<OAuth2AuthScheme>}
     */
    Optional<OAuth2AuthScheme> findAuthScheme(String authSchemeNameOrId);

    /**
     * Fetches a specific {@link OAuth2AuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link OAuth2AuthScheme}, never null
     */
    default OAuth2AuthScheme getAuthScheme(final String authSchemeId) {
        return findAuthScheme(authSchemeId).orElseThrow(AuthSchemeNotFoundException::new);
    }

    /**
     * Updates the supplied {@link OAuth2AuthScheme}
     *
     * @param authScheme the {@link OAuth2AuthScheme} with the information to update the authScheme
     * @return the updated {@link OAuth2AuthScheme} as it was persisted
     */
    OAuth2AuthScheme updateAuthScheme(OAuth2AuthScheme authScheme);

    /**
     * Creates an {@link OAuth2AuthScheme}
     *
     * @param authScheme the {@link OAuth2AuthScheme} with the information to create the authScheme
     * @return the created {@link OAuth2AuthScheme} as it was persisted
     */
    OAuth2AuthScheme createAuthScheme(OAuth2AuthScheme authScheme);

    /**
     * Deletes the {@link OAuth2AuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}