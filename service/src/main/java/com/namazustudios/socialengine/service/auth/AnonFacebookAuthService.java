package com.namazustudios.socialengine.service.auth;

import com.google.common.base.Joiner;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.annotation.FacebookPermissions;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.session.FacebookSessionCreation;
import com.namazustudios.socialengine.service.FacebookAuthService;

import javax.inject.Inject;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.Math.min;

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
                user.setLevel(User.Level.USER);
                user.setActive(true);
                user.setFacebookId(fbUser.getId());
                user.setEmail(fbUser.getEmail());
                user.setName(generateUserName(fbUser));
                return getFacebookUserDao().createReactivateOrUpdateUser(user);
            }
        );
    }

    private String generateUserName(final com.restfb.types.User fbUser) {
        final String firstName = emptyToNull(nullToEmpty(fbUser.getFirstName()).trim().toLowerCase());
        final String middleName = emptyToNull(nullToEmpty(fbUser.getMiddleName()).trim().toLowerCase());
        final String lastName = emptyToNull(nullToEmpty(fbUser.getLastName()).trim().toLowerCase());
        return Joiner.on(".").skipNulls().join(firstName, middleName, lastName, fbUser.getId());
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

