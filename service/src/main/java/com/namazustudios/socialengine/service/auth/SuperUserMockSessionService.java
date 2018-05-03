package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.MockProfileDao;
import com.namazustudios.socialengine.dao.MockUserDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.MockSessionCreation;
import com.namazustudios.socialengine.model.session.MockSessionRequest;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.service.MockSessionService;
import com.namazustudios.socialengine.service.PasswordGenerator;

import javax.inject.Inject;
import javax.inject.Named;

import java.security.SecureRandom;

import static com.namazustudios.socialengine.Constants.MOCK_SESSION_TIMEOUT_SECONDS;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SuperUserMockSessionService implements MockSessionService {

    private static final SecureRandom generator = new SecureRandom();

    private SessionDao sessionDao;

    private MockUserDao mockUserDao;

    private MockProfileDao mockProfileDao;

    private int mockSessionTimeoutSeconds;

    private PasswordGenerator passwordGenerator;

    @Override
    public MockSessionCreation createMockSession(final MockSessionRequest mockSessionRequest) {

        final int timeoutInSeconds = calculateTimeoutInSeconds(mockSessionRequest.getLifetimeInSeconds());
        final long expiry = MILLISECONDS.convert(timeoutInSeconds, SECONDS) + currentTimeMillis();
        final String password = getPasswordGenerator().generate();

        final User user = getMockUserDao().createMockUser(timeoutInSeconds, password);

        Session session = new Session();

        session.setUser(user);
        session.setExpiry(expiry);

        if (mockSessionRequest.getApplication() != null) {
            final Profile p = new Profile();
            p.setUser(user);
            p.setApplication(mockSessionRequest.getApplication());
            session.setProfile(getMockProfileDao().createMockProfile(p));
        }

        final MockSessionCreation mockSessionCreation = new MockSessionCreation();
        final SessionCreation sessionCreation = getSessionDao().create(user, session);

        mockSessionCreation.setSession(session);
        mockSessionCreation.setSessionSecret(mockSessionCreation.getSessionSecret());

        mockSessionCreation.setPassword(password);
        mockSessionCreation.setUserExpiresAt(expiry);

        return mockSessionCreation;

    }

    private int calculateTimeoutInSeconds(final Integer requestedUserExpriration) {
        return requestedUserExpriration == null ?
            getMockSessionTimeoutSeconds() :
            min(requestedUserExpriration, getMockSessionTimeoutSeconds());
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public MockUserDao getMockUserDao() {
        return mockUserDao;
    }

    @Inject
    public void setMockUserDao(MockUserDao mockUserDao) {
        this.mockUserDao = mockUserDao;
    }

    public MockProfileDao getMockProfileDao() {
        return mockProfileDao;
    }

    @Inject
    public void setMockProfileDao(MockProfileDao mockProfileDao) {
        this.mockProfileDao = mockProfileDao;
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

}
