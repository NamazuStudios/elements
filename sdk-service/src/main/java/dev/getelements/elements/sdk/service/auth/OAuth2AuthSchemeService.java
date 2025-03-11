package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOAuth2AuthSchemeRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOAuth2AuthSchemeResponse;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface OAuth2AuthSchemeService {

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
     * Fetches a specific {@link OAuth2AuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link OAuth2AuthScheme}, never null
     */
    OAuth2AuthScheme getAuthScheme(String authSchemeId);

    /**
     * Updates the supplied {@link OAuth2AuthScheme}
     *
     *
     * @param authSchemeId
     * @param authSchemeRequest the {@link CreateOrUpdateOAuth2AuthSchemeRequest} with the information to update the auth scheme.
     *                          Returns error if the auth scheme does not exist.
     * @return a {@link CreateOrUpdateOAuth2AuthSchemeResponse} as it was updated.
     */
    CreateOrUpdateOAuth2AuthSchemeResponse updateAuthScheme(String authSchemeId, CreateOrUpdateOAuth2AuthSchemeRequest authSchemeRequest);

    /**
     * Creates an {@link OAuth2AuthScheme}. Returns error if scheme already exists.
     *
     * @param authSchemeRequest the {@link CreateOrUpdateOAuth2AuthSchemeRequest} with the information to create the auth scheme.
     * @return a {@link CreateOrUpdateOAuth2AuthSchemeResponse} as it was created
     */
    CreateOrUpdateOAuth2AuthSchemeResponse createAuthScheme(CreateOrUpdateOAuth2AuthSchemeRequest authSchemeRequest);

    /**
     * Deletes the {@link OAuth2AuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}
