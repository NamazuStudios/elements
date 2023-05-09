package dev.getelements.elements.service.auth;

import com.google.common.base.Joiner;
import dev.getelements.elements.dao.*;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.FacebookSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import com.restfb.*;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import com.restfb.types.ProfilePictureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

public class FacebookAuthServiceOperations {

    public static final String PROFILE_FIELDS_PARAMETER_VALUE = Joiner.on(",")
            .join("id",
                    "name",
                    "email",
                    "first_name",
                    "last_name",
                    "picture");

    private static final Logger logger = LoggerFactory.getLogger(AnonFacebookAuthService.class);

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private FacebookUserDao facebookUserDao;

    private FacebookFriendDao facebookFriendDao;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    private long sessionTimeoutSeconds;

    public FacebookSessionCreation createOrUpdateUserWithFacebookOAuthAccessToken(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String facebookOAuthAccessToken,
            final Function<com.restfb.types.User, User> userMapper) {
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
                            Parameter.with("fields", PROFILE_FIELDS_PARAMETER_VALUE),
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

            final User user = userMapper.apply(fbUser);

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

            final SessionCreation sessionCreation = getSessionDao().create(session);

            facebookSessionCreation.setSession(sessionCreation.getSession());
            facebookSessionCreation.setSessionSecret(sessionCreation.getSessionSecret());
            facebookSessionCreation.setUserAccessToken(longLivedAccessToken.getAccessToken());

            return facebookSessionCreation;

        });
    }

    private void syncFriendsForUser(final User user,
                                    final FacebookClient facebookClient,
                                    final String appsecretProof) {
        try {
            doSyncFriendsForUser(user, facebookClient, appsecretProof);
        } catch (Exception ex) {
            logger.warn("Failed to sync friends.", ex);
        }
    }

    private void doSyncFriendsForUser(final User user,
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

    private <T> T doFacebookOperation(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (FacebookOAuthException ex) {
            throw new ForbiddenException(ex.getMessage(), ex);
        }
    }

    private Profile map(final User user,
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

    private String generateDisplayName(final com.restfb.types.User fbUser) {

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
