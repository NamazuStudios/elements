package com.namazustudios.socialengine.service.auth;

import com.auritylab.kotlin.apple.signin.AppleIdentityToken;
import com.namazustudios.socialengine.dao.AppleSignInUserDao;
import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.AppleSignInAuthService;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.Level.USER;

public class AnonAppleSignInAuthService implements AppleSignInAuthService {

    private AppleSignInUserDao appleSignInUserDao;

    private AppleSignInAuthServiceOperations appleSignInAuthServiceOperations;

    @Override
    public AppleSignInSessionCreation createOrUpdateUserWithIdentityTokenAndAuthCode(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String identityToken,
            final String authorizationCode) {
        return getAppleSignInAuthServiceOperations().createOrUpdateUserWithAppleSignInTokenAndAuthorizationCode(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                identityToken,
                authorizationCode,
                appleIdentityToken ->  {
                    final User user = new User();
                    user.setActive(true);
                    user.setLevel(USER);
                    user.setEmail(appleIdentityToken.getEmail());
                    user.setName(appleIdentityToken.getUserIdentifier());
                    user.setAppleSignInId(appleIdentityToken.getUserIdentifier());
                    return getAppleSignInUserDao().createReactivateOrUpdateUser(user);
                }
        );
    }

    public AppleSignInUserDao getAppleSignInUserDao() {
        return appleSignInUserDao;
    }

    @Inject
    public void setAppleSignInUserDao(AppleSignInUserDao appleSignInUserDao) {
        this.appleSignInUserDao = appleSignInUserDao;
    }

    public AppleSignInAuthServiceOperations getAppleSignInAuthServiceOperations() {
        return appleSignInAuthServiceOperations;
    }

    @Inject
    public void setAppleSignInAuthServiceOperations(AppleSignInAuthServiceOperations appleSignInAuthServiceOperations) {
        this.appleSignInAuthServiceOperations = appleSignInAuthServiceOperations;
    }

}
