package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.AppleSignInSessionDao;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.applesignin.TokenResponse;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.AppleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoAppleSignInSessionDaoTest {

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private AppleSignInSessionDao appleSignInSessionDao;

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
        application.setName("apple_sign_in_session_test");
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

        final TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("access token");
        tokenResponse.setExpiresIn(300);
        tokenResponse.setIdToken("id token");
        tokenResponse.setTokenType("auth");
        tokenResponse.setRefreshToken("refresh token");

        final Session session = new Session();

        session.setUser(testUser);
        session.setProfile(testProfile);
        session.setApplication(testProfile.getApplication());

        final AppleSignInSessionCreation creation = getAppleSignInSessionDao().create(session, tokenResponse);

        assertEquals(creation.getUserAccessToken(), "access token");

        assertNotNull(creation.getSession());
        assertNotNull(creation.getSessionSecret());
        assertEquals(creation.getSession().getUser(), testUser);
        assertEquals(creation.getSession().getApplication(), testApplication);

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

    public AppleSignInSessionDao getAppleSignInSessionDao() {
        return appleSignInSessionDao;
    }

    @Inject
    public void setAppleSignInSessionDao(AppleSignInSessionDao appleSignInSessionDao) {
        this.appleSignInSessionDao = appleSignInSessionDao;
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
