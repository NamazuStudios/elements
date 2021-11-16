package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.auth.AuthScheme;
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
     * Deletes the {@link AuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);
}
