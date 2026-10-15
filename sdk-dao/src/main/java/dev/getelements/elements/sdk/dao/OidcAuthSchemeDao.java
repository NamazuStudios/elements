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
        value = OidcAuthSchemeDao.OIDC_AUTH_SCHEME_CREATED,
        parameters = OidcAuthScheme.class,
        description = "Called when an OIDC auth scheme was created."
)
@ElementEventProducer(
        value = OidcAuthSchemeDao.OIDC_AUTH_SCHEME_CREATED,
        parameters = {OidcAuthScheme.class, Transaction.class},
        description = "Called when an OIDC auth scheme was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = OidcAuthSchemeDao.OIDC_AUTH_SCHEME_UPDATED,
        parameters = OidcAuthScheme.class,
        description = "Called when an OIDC auth scheme was updated."
)
@ElementEventProducer(
        value = OidcAuthSchemeDao.OIDC_AUTH_SCHEME_UPDATED,
        parameters = {OidcAuthScheme.class, Transaction.class},
        description = "Called when an OIDC auth scheme was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = OidcAuthSchemeDao.OIDC_AUTH_SCHEME_DELETED,
        parameters = OidcAuthScheme.class,
        description = "Called when an OIDC auth scheme was deleted."
)
@ElementEventProducer(
        value = OidcAuthSchemeDao.OIDC_AUTH_SCHEME_DELETED,
        parameters = {OidcAuthScheme.class, Transaction.class},
        description = "Called when an OIDC auth scheme was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface OidcAuthSchemeDao {

    String OIDC_AUTH_SCHEME_CREATED = "dev.getelements.elements.sdk.model.dao.oidc.auth.scheme.created";

    String OIDC_AUTH_SCHEME_UPDATED = "dev.getelements.elements.sdk.model.dao.oidc.auth.scheme.updated";

    String OIDC_AUTH_SCHEME_DELETED = "dev.getelements.elements.sdk.model.dao.oidc.auth.scheme.deleted";

    /**
     * Lists all {@link OidcAuthScheme} instances
     *
     * @param offset
     * @param count
     * @param tags
     * @return a {@link Pagination} of {@link OidcAuthScheme} instances
     */
    Pagination<OidcAuthScheme> getAuthSchemes(int offset, int count, List<String> tags);

    /**
     * Finds an {@link OidcAuthScheme}, returning an {@link Optional}.
     *
     * @param authSchemeIssuerNameOrId the auth scheme id
     * @return an {@link Optional<OidcAuthScheme>}
     */
    Optional<OidcAuthScheme> findAuthScheme(String authSchemeIssuerNameOrId);

    /**
     * Fetches a specific {@link OidcAuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link OidcAuthScheme}, never null
     */
    default OidcAuthScheme getAuthScheme(final String authSchemeId) {
        return findAuthScheme(authSchemeId).orElseThrow(AuthSchemeNotFoundException::new);
    }

    /**
     * Updates the supplied {@link OidcAuthScheme}
     *
     * @param authScheme the {@link OidcAuthScheme} with the information to update the authScheme
     * @return the updated {@link OidcAuthScheme} as it was persisted
     */
    OidcAuthScheme updateAuthScheme(OidcAuthScheme authScheme);

    /**
     * Creates an {@link OidcAuthScheme}
     *
     * @param authScheme the {@link OidcAuthScheme} with the information to create the authScheme
     * @return the created {@link OidcAuthScheme} as it was persisted
     */
    OidcAuthScheme createAuthScheme(OidcAuthScheme authScheme);

    /**
     * Deletes the {@link OidcAuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}