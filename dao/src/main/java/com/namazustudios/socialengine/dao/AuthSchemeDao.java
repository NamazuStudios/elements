package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.auth.*;

import java.util.List;

public interface AuthSchemeDao {
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
     * @param authSchemeRequest the {@link UpdateAuthSchemeRequest} with the information to update the authScheme
     * @return a {@link UpdateAuthSchemeResponse} as it was created
     */
    UpdateAuthSchemeResponse updateAuthScheme(UpdateAuthSchemeRequest authSchemeRequest);

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
