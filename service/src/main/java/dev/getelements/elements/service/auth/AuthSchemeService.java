package dev.getelements.elements.service.auth;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.auth.AuthScheme;
import dev.getelements.elements.model.auth.CreateAuthSchemeRequest;
import dev.getelements.elements.model.auth.CreateAuthSchemeResponse;
import dev.getelements.elements.model.auth.UpdateAuthSchemeRequest;
import dev.getelements.elements.model.auth.UpdateAuthSchemeResponse;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link AuthScheme}.
 *
 * Created by robb on 11/12/21.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.auth.authscheme"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.auth.unscoped.authscheme",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.auth.authscheme",
                deprecated = @DeprecationDefinition("Use eci.elements.service.auth.authscheme instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.auth.unscoped.authscheme",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.auth.unscoped.authscheme instead.")
        )
})
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
