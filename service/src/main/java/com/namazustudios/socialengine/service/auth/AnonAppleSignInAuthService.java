package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.AppleSignInUserDao;
import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.AppleSignInAuthService;
import com.namazustudios.socialengine.service.auth.AppleSignInAuthServiceOperations.Claim;

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
                user.setName(appleIdentityToken.getClaim(Claim.USER_ID.value).asString());
                user.setEmail(appleIdentityToken.getClaim(Claim.EMAIL.value).asString());
                user.setAppleSignInId(appleIdentityToken.getClaim(Claim.USER_ID.value).asString());
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
