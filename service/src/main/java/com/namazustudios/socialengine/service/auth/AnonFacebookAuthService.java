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

    protected static final Logger logger = LoggerFactory.getLogger(AnonFacebookAuthService.class);

    protected static final String FIELDS_PARAMETER_VALUE = Joiner.on(",")
        .join("id","name","email","first_name","last_name","picture");

    protected ProfileDao profileDao;

    protected SessionDao sessionDao;

    protected FacebookUserDao facebookUserDao;

    protected FacebookFriendDao facebookFriendDao;

    protected FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    protected long sessionTimeoutSeconds;

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

            final User user = getFacebookUserDao().createReactivateOrUpdateUser(map(fbUser));
            final Profile profile = getProfileDao().createOrRefreshProfile(map(
                    user,
                    fbUser,
                    facebookApplicationConfiguration,
                    profilePictureSource));

            final Session session = new Session();
            final FacebookSessionCreation facebookSessionCreation = new FacebookSessionCreation();
            final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

            syncFriendsForUser(user, facebookClient, appsecretProof);

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

    protected void syncFriendsForUser(final User user,
                                    final FacebookClient facebookClient,
                                    final String appsecretProof) {
        try {
            doSyncFriendsForUser(user, facebookClient, appsecretProof);
        } catch (Exception ex) {
            logger.warn("Failed to sync friends.", ex);
        }
    }

    protected void doSyncFriendsForUser(final User user,
                                      final FacebookClient facebookClient,
                                      final String appsecretProof) {

        final Connection<com.restfb.types.User> userConnection = facebookClient
            .fetchConnection(
                "me/friends",
                com.restfb.types.User.class,
                Parameter.with("appsecret_proof", appsecretProof));

        for (final List<com.restfb.types.User> userList : userConnection) {
            getFacebookFriendDao().associateFriends(user, userList.stream().map(u -> u.getId()).collect(toList()));
        }

    }

    protected <T> T doFacebookOperation(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (FacebookOAuthException ex) {
            throw new ForbiddenException(ex.getMessage(), ex);
        }
    }

    protected User map(final com.restfb.types.User fbUser) {
        final User user = new User();
        user.setLevel(User.Level.USER);
        user.setActive(true);
        user.setFacebookId(fbUser.getId());
        user.setEmail(fbUser.getEmail());
        user.setName(generateUserName(fbUser));
        return user;
    }

    protected String generateUserName(final com.restfb.types.User fbUser) {
        final String firstName = emptyToNull(nullToEmpty(fbUser.getFirstName()).trim().toLowerCase());
        final String middleName = emptyToNull(nullToEmpty(fbUser.getMiddleName()).trim().toLowerCase());
        final String lastName = emptyToNull(nullToEmpty(fbUser.getLastName()).trim().toLowerCase());
        return Joiner.on(".").skipNulls().join(firstName, middleName, lastName, fbUser.getId());
    }

    protected Profile map(final User user,
                        final com.restfb.types.User fbUser,
                        final FacebookApplicationConfiguration facebookApplicationConfiguration,
                        final ProfilePictureSource profilePictureSource) {
        final Profile profile = new Profile();
        profile.setUser(user);
        profile.setApplication(facebookApplicationConfiguration.getParent());
        profile.setDisplayName(generateDisplayName(fbUser));
        profile.setImageUrl(profilePictureSource.getUrl());
        return profile;
    }

    protected String generateDisplayName(final com.restfb.types.User fbUser) {

        final String firstName = nullToEmpty(fbUser.getFirstName()).trim();
        final String lastName = nullToEmpty(fbUser.getLastName()).trim();

        if (lastName.isEmpty()) {
            return firstName;
        } else {
            final String lastInitial = lastName.substring(0, min(1, lastName.length())) + ".";
            return Joiner.on(" ").join(firstName, lastInitial);
        }

    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public FacebookUserDao getFacebookUserDao() {
        return facebookUserDao;
    }

    @Inject
    public void setFacebookUserDao(FacebookUserDao facebookUserDao) {
        this.facebookUserDao = facebookUserDao;
    }

    public FacebookFriendDao getFacebookFriendDao() {
        return facebookFriendDao;
    }

    @Inject
    public void setFacebookFriendDao(FacebookFriendDao facebookFriendDao) {
        this.facebookFriendDao = facebookFriendDao;
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
