package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.AppleSignInUserDao;
import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.AppleSignInAuthService;

import javax.inject.Inject;

public class UserAppleSignInAuthService implements AppleSignInAuthService {

    private User user;

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
                user.setId(getUser().getId());
                user.setLevel(getUser().getLevel());
                user.setActive(getUser().isActive());
                user.setAppleSignInId(appleIdentityToken.getUserIdentifier());
                user.setEmail(getUser().getEmail());
                user.setName(getUser().getName());
                return getAppleSignInUserDao().connectActiveAppleUserIfNecessary(user);
            }
        );
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
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
