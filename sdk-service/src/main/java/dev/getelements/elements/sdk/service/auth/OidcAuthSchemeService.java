package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcAuthSchemeRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcAuthSchemeResponse;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface OidcAuthSchemeService {

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
     * Fetches a specific {@link OidcAuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link OidcAuthScheme}, never null
     */
    OidcAuthScheme getAuthScheme(String authSchemeId);

    /**
     * Updates the supplied {@link OidcAuthScheme}
     *
     *
     * @param authSchemeId
     * @param authSchemeRequest the {@link CreateOrUpdateOidcAuthSchemeRequest} with the information to update the auth scheme.
     *                          Returns error if the auth scheme does not exist.
     * @return a {@link CreateOrUpdateOidcAuthSchemeResponse} as it was updated.
     */
    CreateOrUpdateOidcAuthSchemeResponse updateAuthScheme(String authSchemeId, CreateOrUpdateOidcAuthSchemeRequest authSchemeRequest);

    /**
     * Creates an {@link OidcAuthScheme}. Returns error if scheme already exists.
     *
     * @param authSchemeRequest the {@link CreateOrUpdateOidcAuthSchemeRequest} with the information to create the auth scheme.
     * @return a {@link CreateOrUpdateOidcAuthSchemeResponse} as it was created
     */
    CreateOrUpdateOidcAuthSchemeResponse createAuthScheme(CreateOrUpdateOidcAuthSchemeRequest authSchemeRequest);

    /**
     * Deletes the {@link OidcAuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}
