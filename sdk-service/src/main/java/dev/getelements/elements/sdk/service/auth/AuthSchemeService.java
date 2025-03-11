package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.*;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages instances of {@link AuthScheme}.
 *
 * Created by robb on 11/12/21.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface AuthSchemeService {

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
     * Fetches a specific {@link AuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link AuthScheme}, never null
     */
    AuthScheme getAuthScheme(String authSchemeId);

    /**
     * Updates the supplied {@link AuthScheme}
     *
     *
     * @param authSchemeId
     * @param authSchemeRequest the {@link UpdateAuthSchemeRequest} with the information to update the authScheme
     * @return a {@link UpdateAuthSchemeResponse} as it was created
     */
    UpdateAuthSchemeResponse updateAuthScheme(String authSchemeId, UpdateAuthSchemeRequest authSchemeRequest);

    /**
     * Creates an {@link AuthScheme}
     *
     * @param authSchemeRequest the {@link CreateAuthSchemeRequest} with the information to create the authScheme
     * @return a {@link CreateAuthSchemeResponse} as it was created
     */
    CreateAuthSchemeResponse createAuthScheme(CreateAuthSchemeRequest authSchemeRequest);

    /**
     * Deletes the {@link AuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}
