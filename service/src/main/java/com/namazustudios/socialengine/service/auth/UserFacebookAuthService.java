package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.session.FacebookSessionCreation;
import com.namazustudios.socialengine.service.FacebookAuthService;

public class UserFacebookAuthService implements FacebookAuthService {
    @Override
    public FacebookSessionCreation createOrUpdateUserWithFacebookOAuthAccessToken(String applicationNameOrId, String applicationConfigurationNameOrId, String facebookOAuthAccessToken) {
        return null;
    }
}
