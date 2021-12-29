package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.auth.AuthSchemeNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.auth.*;

import java.util.List;
import java.util.Optional;

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
