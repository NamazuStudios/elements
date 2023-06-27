package dev.getelements.elements.service.auth;

import dev.getelements.elements.annotation.FacebookPermission;
import dev.getelements.elements.annotation.FacebookPermissions;
import dev.getelements.elements.dao.*;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.session.FacebookSessionCreation;
import dev.getelements.elements.service.FacebookAuthService;

import javax.inject.Inject;

import static java.lang.Math.min;

/**
 * This is the user-scope {@link FacebookAuthService} used
 *
 * Created by davidjbrooks on 12/12/2018.
 */
@FacebookPermissions({
        @FacebookPermission("email"),
        @FacebookPermission("public_profile"),
        @FacebookPermission("user_friends")
})
public class UserFacebookAuthService extends AnonFacebookAuthService implements FacebookAuthService {

    private User user;

    private FacebookUserDao facebookUserDao;

    private FacebookAuthServiceOperations facebookAuthServiceOperations;

    @Override
    public FacebookSessionCreation createOrUpdateUserWithFacebookOAuthAccessToken(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String facebookOAuthAccessToken) {
        return getFacebookAuthServiceOperations().createOrUpdateUserWithFacebookOAuthAccessToken(
            applicationNameOrId,
            applicationConfigurationNameOrId,
            facebookOAuthAccessToken,
            (fbUser) -> {
                final User user = new User();
                user.setId(getUser().getId());
                user.setLevel(getUser().getLevel());
                user.setActive(getUser().isActive());
                user.setFacebookId(fbUser.getId());
                user.setEmail(getUser().getEmail());
                user.setName(getUser().getName());
                return getFacebookUserDao().connectActiveUserIfNecessary(user);
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

    public FacebookUserDao getFacebookUserDao() {
        return facebookUserDao;
    }

    @Inject
    public void setFacebookUserDao(FacebookUserDao facebookUserDao) {
        this.facebookUserDao = facebookUserDao;
    }

    public FacebookAuthServiceOperations getFacebookAuthServiceOperations() {
        return facebookAuthServiceOperations;
    }

    @Inject
    public void setFacebookAuthServiceOperations(FacebookAuthServiceOperations facebookAuthServiceOperations) {
        this.facebookAuthServiceOperations = facebookAuthServiceOperations;
    }

}
