package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.session.SessionCreation;

/**
 * Used to authorize requests users using Facebook Apple Sign-in tokens.
 *
 * Created by patricktwohig on 6/22/17.
 */
public interface AppleSignInAuthService {

    /**
     * Creates a new session using the supplied application, application configuration, and the identity token.
     *
     * In addition to manipulating the user account, this should also convert the short-term facebook token
     * to a long-term token and supply the result.
     *
     * @param applicationNameOrId the application name or id
     * @param applicationConfigurationNameOrId the application configuration name or id
     * @param identityToken the identity token issued by Apple's services
     * @param authorizationCode the authorization code issued by Apple's services
     *
     * @return
     */
    AppleSignInSessionCreation createOrUpdateUserWithIdentityTokenAndAuthCode(String applicationNameOrId,
                                                                              String applicationConfigurationNameOrId,
                                                                              String identityToken,
                                                                              String authorizationCode);

}
