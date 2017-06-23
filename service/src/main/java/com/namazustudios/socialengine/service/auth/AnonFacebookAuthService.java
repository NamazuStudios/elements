package com.namazustudios.socialengine.service.auth;

import com.google.common.base.Joiner;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.annotation.FacebookPermissions;
import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.session.FacebookSession;
import com.namazustudios.socialengine.service.FacebookAuthService;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

import javax.inject.Inject;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * This is the basic {@link FacebookAuthService} used
 *
 * Created by patricktwohig on 6/22/17.
 */
@FacebookPermissions({
    @FacebookPermission("email"),
    @FacebookPermission("public_profile"),
    @FacebookPermission("user_friends")
})
public class AnonFacebookAuthService implements FacebookAuthService {

    private UserDao userDao;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    @Override
    public FacebookSession createOrUpdateUserWithFacebookOAuthAccessToken(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String facebookOAuthAccessToken) {

        final FacebookApplicationConfiguration facebookApplicationConfiguration =
            getFacebookApplicationConfigurationDao()
                .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);

        final FacebookClient facebookClient = new DefaultFacebookClient(facebookOAuthAccessToken, Version.LATEST);

        final com.restfb.types.User fbUser = facebookClient.fetchObject("me", com.restfb.types.User.class);
        final User user = getUserDao().createOrActivateUser(map(fbUser));

        final FacebookClient.AccessToken longLivedAccessToken;
        longLivedAccessToken = facebookClient.obtainExtendedAccessToken(
            facebookApplicationConfiguration.getApplicationId(),
            facebookApplicationConfiguration.getApplicationSecret());

        final FacebookSession facebookSession = new FacebookSession();

        facebookSession.setUser(user);
        facebookSession.setLongLivedToken(longLivedAccessToken.getAccessToken());

        return facebookSession;

    }

    private User map(final com.restfb.types.User fbUser) {
        final User user = new User();
        user.setLevel(User.Level.USER);
        user.setActive(true);
        user.setFacebookId(fbUser.getId());
        user.setEmail(fbUser.getEmail());
        user.setName(generateUserName(fbUser));
        return user;
    }

    private String generateUserName(final com.restfb.types.User fbUser) {
        final String firstName = emptyToNull(nullToEmpty(fbUser.getFirstName()).trim());
        final String middleName = emptyToNull(nullToEmpty(fbUser.getMiddleName()).trim());
        final String lastName = emptyToNull(nullToEmpty(fbUser.getLastName().trim()));
        return Joiner.on(".").skipNulls().join(firstName, middleName, lastName);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

}
