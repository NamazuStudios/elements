package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.AppleSignInAuthService;

public class AnonAppleSignInAuthService implements AppleSignInAuthService {

    @Override
    public AppleSignInSessionCreation createOrUpdateUserWithIdentityTokenAndAuthCode(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String identityToken,
            final String authorizationCode) {
        // TODO: Verify against the Apple REST APIs for the session information
        final Session session = new Session();
        final AppleSignInSessionCreation sessionCreation = new AppleSignInSessionCreation();
        sessionCreation.setSession(session);
        sessionCreation.setSessionSecret("mock");
        return sessionCreation;
    }

}
