package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.session.UsernamePasswordSessionRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Objects;
import java.util.Optional;

import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by patricktwohig on 4/1/15.
 */
@Singleton
public class AnonUsernamePasswordAuthService implements UsernamePasswordAuthService {

    private UserDao userDao;

    private SessionDao sessionDao;

    private ProfileDao profileDao;

    private ValidationHelper validationHelper;

    private long sessionTimeoutSeconds;

    @Override
    public SessionCreation createSession(final UsernamePasswordSessionRequest usernamePasswordSessionRequest) {

        getValidationHelper().validateModel(usernamePasswordSessionRequest);

        final var userId = usernamePasswordSessionRequest.getUserId().trim();
        final var password = usernamePasswordSessionRequest.getPassword();
        final var profileId = usernamePasswordSessionRequest.getProfileId();
        final var profileSelector = usernamePasswordSessionRequest.getProfileSelector();

        final var user = getUserDao().validateUserPassword(userId, password);
        final var profile = getProfileIfSpecified(profileId).or(() -> selectProfileIfSpecified(user, profileSelector));

        profile.ifPresent(p -> {
            if (!Objects.equals(user, p.getUser())) {
                throw new ForbiddenException("Invalid credentials for " + userId);
            }
        });

        final var session = new Session();

        session.setUser(user);
        session.setProfile(profile.orElse(null));
        profile.map(Profile::getApplication).ifPresent(session::setApplication);

        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        session.setExpiry(expiry);

        return getSessionDao().create(session);

    }

    private Optional<Profile> getProfileIfSpecified(final String profileId) {
        return profileId == null ?
                Optional.empty() :
                Optional.of(getProfileDao().getActiveProfile(profileId));
    }

    private Optional<Profile> selectProfileIfSpecified(final User user, final String selector) {

        if (selector == null) {
            return Optional.empty();
        }

        final var query = String.format(".ref.user:%s AND %s", user.getId(), selector);

        final var profiles = getProfileDao()
                .getActiveProfiles(0, 1, query)
                .getObjects();

        if (profiles.isEmpty()) {
            throw new ProfileNotFoundException("Profile not found.");
        }

        return Optional.of(profiles.get(0));

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
