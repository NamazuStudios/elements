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
     * Lists all {@link AuthScheme} instances
     *
     * @param offset
     * @param count
     * @param tags
     * @return a {@link Pagination} of {@link AuthScheme} instances
     */
    Pagination<OidcAuthScheme> getAuthSchemes(int offset, int count, List<String> tags);

    /**
     * Finds an {@link AuthScheme}, returning an {@link Optional}.
     *
     * @param authSchemeIssuerNameOrId the auth scheme id
     * @return an {@link Optional<AuthScheme>}
     */
    Optional<OidcAuthScheme> findAuthScheme(String authSchemeIssuerNameOrId);

    /**
     * Fetches a specific {@link AuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link AuthScheme}, never null
     */
    default OidcAuthScheme getAuthScheme(final String authSchemeId) {
        return findAuthScheme(authSchemeId).orElseThrow(AuthSchemeNotFoundException::new);
    }

    /**
     * Updates the supplied {@link AuthScheme}
     *
     * @param authScheme the {@link UpdateAuthSchemeRequest} with the information to update the authScheme
     * @return a {@link UpdateAuthSchemeResponse} as it was created
     */
    OidcAuthScheme updateAuthScheme(OidcAuthScheme authScheme);

    /**
     * Creates an {@link AuthScheme}
     *
     * @param authScheme the {@link CreateAuthSchemeRequest} with the information to create the authScheme
     * @return a {@link CreateAuthSchemeResponse} as it was created
     */
    OidcAuthScheme createAuthScheme(OidcAuthScheme authScheme);

    /**
     * Deletes the {@link AuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}