package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Used to authorize requests using JWT auth tokens
 *
 * Created by robb on 12/20/21.
 */
@Expose({
    @ExposedModuleDefinition(
        value = "namazu.elements.service.auth.unscoped.custom.auth",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )}
)
public interface CustomAuthSessionService {

    /**
     * Gets a {@link Session} provided the custom auth JWT.
     *
     * @param jwt the jwt
     * @return the the {@link Session} instance
     */
    Session getSession(final String jwt);

}
