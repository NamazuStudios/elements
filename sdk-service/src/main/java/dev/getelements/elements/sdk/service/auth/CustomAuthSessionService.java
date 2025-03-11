package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Used to authorize requests using JWT auth tokens
 *
 * Created by robb on 12/20/21.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface CustomAuthSessionService {

    /**
     * Gets a {@link Session} provided the custom auth JWT.
     *
     * @param jwt the jwt
     * @return the {@link Session} instance
     */
    Session getSession(final String jwt);

}
