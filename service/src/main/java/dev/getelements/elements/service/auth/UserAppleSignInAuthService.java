package dev.getelements.elements.service.auth;

import dev.getelements.elements.dao.AppleSignInUserDao;
import dev.getelements.elements.model.session.AppleSignInSessionCreation;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.AppleSignInAuthService;
import dev.getelements.elements.service.auth.AppleSignInAuthServiceOperations.Claim;

import javax.inject.Inject;

public class UserAppleSignInAuthService implements AppleSignInAuthService {

    private User user;

    private AppleSignInUserDao appleSignInUserDao;

    private AppleSignInAuthServiceOperations appleSignInAuthServiceOperations;

    @Override
    public AppleSignInSessionCreation createOrUpdateUserWithIdentityTokenAndAuthCode(
            final String applicationNameOrId,
            final String identityToken) {

        return getAppleSignInAuthServiceOperations().createOrUpdateUserWithAppleSignInToken(
            applicationNameOrId,
            identityToken,
            appleIdentityToken ->  {
                final User user = new User();
                user.setId(getUser().getId());
                user.setLevel(getUser().getLevel());
                user.setActive(getUser().isActive());
                user.setAppleSignInId(appleIdentityToken.getClaim(Claim.USER_ID.value).asString());
                user.setEmail(getUser().getEmail());
                user.setName(getUser().getName());
                return getAppleSignInUserDao().connectActiveUserIfNecessary(user);
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
