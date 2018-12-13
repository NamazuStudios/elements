package com.namazustudios.socialengine.service.auth;

import com.google.common.base.Joiner;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.annotation.FacebookPermissions;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.FacebookSessionCreation;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.service.FacebookAuthService;
import com.restfb.*;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import com.restfb.types.ProfilePictureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

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

    @Override
    public FacebookSessionCreation createOrUpdateUserWithFacebookOAuthAccessToken(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String facebookOAuthAccessToken) {

        return doFacebookOperation(() -> {

            final FacebookApplicationConfiguration facebookApplicationConfiguration =
                getFacebookApplicationConfigurationDao()
                    .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);


            final FacebookClient facebookClient = new DefaultFacebookClient(facebookOAuthAccessToken, Version.LATEST);

            final FacebookClient.AccessToken longLivedAccessToken;
            longLivedAccessToken = facebookClient.obtainExtendedAccessToken(
                facebookApplicationConfiguration.getApplicationId(),
                facebookApplicationConfiguration.getApplicationSecret());

            final String appsecretProof = facebookClient.obtainAppSecretProof(
                facebookOAuthAccessToken,
                facebookApplicationConfiguration.getApplicationSecret());

            final com.restfb.types.User fbUser = facebookClient
                .fetchObject(
                    "me",
                    com.restfb.types.User.class,
                    Parameter.with("fields", FIELDS_PARAMETER_VALUE),
                    Parameter.with("appsecret_proof", appsecretProof));

            final JsonObject rawProfilePicture = facebookClient
                .fetchObject(
                    "me/picture",
                    JsonObject.class,
                    Parameter.with("type", "large"),
                    Parameter.with("redirect", false),
                    Parameter.with("appsecret_proof", appsecretProof));

            final ProfilePictureSource profilePictureSource = facebookClient
                .getJsonMapper()
                .toJavaObject(rawProfilePicture.get("data").toString(), ProfilePictureSource.class);


            // check to ensure facebook ID isn't already assigned to another account



            final User user = getFacebookUserDao().createReactivateOrUpdateUser(map(fbUser));
            final Profile profile = getProfileDao().createOrRefreshProfile(map(
                user,
                fbUser,
                facebookApplicationConfiguration,
                profilePictureSource));


            syncFriendsForUser(user, facebookClient, appsecretProof);

            final Session session = new Session();
            final FacebookSessionCreation facebookSessionCreation = new FacebookSessionCreation();
            final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

            session.setUser(user);
            session.setProfile(profile);
            session.setApplication(facebookApplicationConfiguration.getParent());
            session.setExpiry(expiry);

            final SessionCreation sessionCreation = getSessionDao().create(user, session);

            facebookSessionCreation.setSession(sessionCreation.getSession());
            facebookSessionCreation.setSessionSecret(sessionCreation.getSessionSecret());
            facebookSessionCreation.setUserAccessToken(longLivedAccessToken.getAccessToken());

            return facebookSessionCreation;

        });
    }

}
