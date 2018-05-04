package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.MockSessionCreation;
import com.namazustudios.socialengine.model.session.MockSessionRequest;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.service.MockSessionService;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.util.DisplayNameGenerator;

import javax.inject.Inject;
import javax.inject.Named;

import static com.namazustudios.socialengine.Constants.MOCK_SESSION_TIMEOUT_SECONDS;
import static com.namazustudios.socialengine.model.User.Level.USER;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SuperUserMockSessionService implements MockSessionService {

    private UserDao userDao;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private int mockSessionTimeoutSeconds;

    private PasswordGenerator passwordGenerator;

    private DisplayNameGenerator displayNameGenerator;

    @Override
    public MockSessionCreation createMockSession(final MockSessionRequest mockSessionRequest) {

        final int timeoutInSeconds = calculateTimeoutInSeconds(mockSessionRequest.getLifetimeInSeconds());
        final long expiry = MILLISECONDS.convert(timeoutInSeconds, SECONDS) + currentTimeMillis();
        final String password = getPasswordGenerator().generate();

        final User user = generateUser(password);
        Session session = new Session();

        session.setUser(user);
        session.setExpiry(expiry);

        if (mockSessionRequest.getApplication() != null) {
            final Profile profile = generateProfile(user, mockSessionRequest.getApplication());
            session.setProfile(profile);
        }

        final MockSessionCreation mockSessionCreation = new MockSessionCreation();
        final SessionCreation sessionCreation = getSessionDao().create(user, session);

        mockSessionCreation.setSession(sessionCreation.getSession());
        mockSessionCreation.setSessionSecret(sessionCreation.getSessionSecret());

        mockSessionCreation.setPassword(password);
        mockSessionCreation.setUserExpiresAt(expiry);

        return mockSessionCreation;

    }

    private int calculateTimeoutInSeconds(final Integer requestedUserExpriration) {
        return requestedUserExpriration == null ?
            getMockSessionTimeoutSeconds() :
            min(requestedUserExpriration, getMockSessionTimeoutSeconds());
    }

    private User generateUser(final String password) {
        final User user = new User();
        user.setName(format("test-user-%s", randomUUID()));
        user.setEmail(format("%s@example.com", user.getName()));
        user.setActive(true);
        user.setLevel(USER);
        return getUserDao().createOrRectivateUserWithPassword(user, password);
    }

    private Profile generateProfile(final User user, final Application application) {
        final Profile profile = new Profile();
        profile.setUser(user);
        profile.setApplication(application);
        profile.setDisplayName(getDisplayNameGenerator().generate());
        return getProfileDao().createOrReactivateProfile(profile);
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public int getMockSessionTimeoutSeconds() {
        return mockSessionTimeoutSeconds;
    }

    @Inject
    public void setMockSessionTimeoutSeconds(@Named(MOCK_SESSION_TIMEOUT_SECONDS) int mockSessionTimeoutSeconds) {
        this.mockSessionTimeoutSeconds = mockSessionTimeoutSeconds;
    }

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public DisplayNameGenerator getDisplayNameGenerator() {
        return displayNameGenerator;
    }

    @Inject
    public void setDisplayNameGenerator(DisplayNameGenerator displayNameGenerator) {
        this.displayNameGenerator = displayNameGenerator;
    }

}

