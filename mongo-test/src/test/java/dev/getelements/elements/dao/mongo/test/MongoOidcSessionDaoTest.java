package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonMap;
import static java.time.LocalDateTime.now;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoOidcSessionDaoTest {

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private SessionDao sessionDao;

    private ProfileDao profileDao;

    private Application testApplication;

    private User testUser;

    private Profile testProfile;

    private UserTestFactory userTestFactory;

    @BeforeClass
    public void setup() {
        testApplication = makeTestApplication();
        testUser = getUserTestFactory().createTestUser();
        testProfile = buildTestProfile();
    }

    public Application makeTestApplication() {
        final Application application = new Application();
        application.setName("sign_in_session_test");
        application.setDescription("A mock application.");
        application.setAttributes(singletonMap("key", "value"));
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    private Profile buildTestProfile() {
        final Profile profile = new Profile();
        profile.setUser(testUser);
        profile.setApplication(testApplication);
        profile.setDisplayName(format("display-name-%s", testUser.getName()));
        profile.setImageUrl(format("http://example.com/%s.png", testUser.getName()));
        return getProfileDao().createOrReactivateProfile(profile);
    }

    @Test
    public void testCreate() {

        final Session session = new Session();
        final var expiry = currentTimeMillis() + 1000;

        session.setUser(testUser);
        session.setProfile(testProfile);
        session.setApplication(testProfile.getApplication());
        session.setExpiry(expiry);

        final SessionCreation creation = getSessionDao().create(session);

        assertNotNull(creation.getSession());
        assertNotNull(creation.getSessionSecret());
        assertEquals(creation.getSession().getUser(), testUser);
        assertEquals(creation.getSession().getApplication(), testApplication);

    }

    @Test(expectedExceptions = InvalidDataException.class)
    public void testCreateNoExpiry() {

        final Session session = new Session();

        session.setUser(testUser);
        session.setProfile(testProfile);
        session.setApplication(testProfile.getApplication());

        getSessionDao().create(session);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
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

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
