package com.namazustudios.socialengine.service.auth;

import com.google.common.base.Joiner;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.annotation.FacebookPermissions;
import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.FacebookSessionCreation;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.FacebookAuthService;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonObject;
import com.restfb.types.ProfilePictureSource;

import javax.inject.Inject;
import java.util.function.Supplier;

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
    @FacebookPermission("public_profile")
})
public class StandardFacebookAuthService implements FacebookAuthService {


    private static final String FIELDS_PARAMETER_VALUE = Joiner.on(",")
        .join("id","name","email","first_name","last_name","picture");

    private ProfileDao profileDao;

    private FacebookUserDao facebookUserDao;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    @Override
    public FacebookSessionCreation authenticate(String applicationConfigurationNameOrId,
                                                String facebookOAuthAccessToken) {
        return doFacebookOperation(() -> {

            final FacebookApplicationConfiguration facebookApplicationConfiguration =
                    getFacebookApplicationConfigurationDao()
                            .getApplicationConfiguration(applicationConfigurationNameOrId);

            final FacebookClient facebookClient = new DefaultFacebookClient(facebookOAuthAccessToken, Version.LATEST);

            final String appsecretProof = facebookClient.obtainAppSecretProof(
                    facebookOAuthAccessToken,
                    facebookApplicationConfiguration.getApplicationSecret());

            final com.restfb.types.User fbUser = facebookClient
                    .fetchObject(
                            "me",
                            com.restfb.types.User.class,
                            Parameter.with("fields", FIELDS_PARAMETER_VALUE),
                            Parameter.with("appsecret_proof", appsecretProof));

            try {

                final Session session = new Session();
                final FacebookSessionCreation facebookSession = new FacebookSessionCreation();

                final User user = getFacebookUserDao().findActiveByFacebookId(fbUser.getId());
                final Profile profile = getProfileDao().getActiveProfile(
                    user.getId(),
                    facebookApplicationConfiguration.getParent().getId());

                session.setUser(user);
                session.setProfile(profile);
                session.setApplication(facebookApplicationConfiguration.getParent());

                facebookSession.setSession(session);
                facebookSession.setUserAccessToken(facebookOAuthAccessToken);

                return facebookSession;

            } catch (NotFoundException ex) {
                throw new ForbiddenException(ex);
            }

        });
    }

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
            final Profile profile = getProfileDao().createReactivateOrRefreshProfile(map(
                    user,
                    fbUser,
                    facebookApplicationConfiguration,
                    profilePictureSource));

            final Session session = new Session();
            final FacebookSessionCreation facebookSession = new FacebookSessionCreation();

            session.setUser(user);
            session.setProfile(profile);
            session.setApplication(facebookApplicationConfiguration.getParent());

            facebookSession.setSession(session);
            facebookSession.setUserAccessToken(facebookOAuthAccessToken);

            return facebookSession;

        });
    }

    private <T> T doFacebookOperation(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (FacebookOAuthException ex) {
            throw new ForbiddenException(ex.getMessage(), ex);
        }
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
        final String firstName = emptyToNull(nullToEmpty(fbUser.getFirstName()).trim().toLowerCase());
        final String middleName = emptyToNull(nullToEmpty(fbUser.getMiddleName()).trim().toLowerCase());
        final String lastName = emptyToNull(nullToEmpty(fbUser.getLastName()).trim().toLowerCase());
        return Joiner.on(".").skipNulls().join(firstName, middleName, lastName, fbUser.getId());
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

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

}
