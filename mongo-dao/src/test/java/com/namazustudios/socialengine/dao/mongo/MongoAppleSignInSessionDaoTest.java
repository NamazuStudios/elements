package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.AppleSignInSessionDao;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.applesignin.TokenResponse;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static java.lang.String.format;
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

    @BeforeClass
    public void setup() {
        testApplication = makeTestApplication();
        testUser = buildTestUser();
        testProfile = buildTestProfile();
    }

    public Application makeTestApplication() {
        final Application application = new Application();
        application.setName("mock");
        application.setDescription("A mock application.");
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    private User buildTestUser() {
        final User testUser = new User();
        testUser.setName("testy.mctesterson.4");
        testUser.setEmail("testy.mctesterson.4@example.com");
        testUser.setLevel(USER);
        return getUserDao().createOrReactivateUser(testUser);
    }

    private Profile buildTestProfile() {
        final CreateProfileRequest createProfileRequest =  new CreateProfileRequest();
        createProfileRequest.setUserId(testUser.getId());
        createProfileRequest.setApplicationId(testApplication.getId());
        createProfileRequest.setDisplayName(format("display-name-%s", testUser.getName()));
        createProfileRequest.setImageUrl(format("http://example.com/%s.png", testUser.getName()));
        return getProfileDao().createOrReactivateProfile(createProfileRequest);
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

}
