package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.auth.AuthScheme;
import com.namazustudios.socialengine.model.auth.CreateAuthSchemeRequest;
import com.namazustudios.socialengine.model.auth.CreateAuthSchemeResponse;
import com.namazustudios.socialengine.model.auth.UpdateAuthSchemeRequest;
import com.namazustudios.socialengine.model.auth.UpdateAuthSchemeResponse;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link AuthScheme}.
 *
 * Created by robb on 11/12/21.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.auth.authscheme"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.auth.unscoped.authscheme",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )})
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
