package dev.getelements.elements.service.auth;

import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.SessionDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.MockSessionCreation;
import dev.getelements.elements.model.session.MockSessionRequest;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.session.SessionCreation;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.PasswordGenerator;
import dev.getelements.elements.service.MockSessionService;
import dev.getelements.elements.service.NameService;

import javax.inject.Inject;
import javax.inject.Named;

import static dev.getelements.elements.Constants.MOCK_SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.model.user.User.Level.USER;
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

    private NameService nameService;

    private PasswordGenerator passwordGenerator;

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
        final SessionCreation sessionCreation = getSessionDao().create(session);

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
        return getUserDao().createOrReactivateUserWithPassword(user, password);
    }

    private Profile generateProfile(final User user, final Application application) {
        final Profile profile = new Profile();
        profile.setUser(user);
        profile.setApplication(application);
        profile.setDisplayName(getNameService().generateRandomName());
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

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

}

