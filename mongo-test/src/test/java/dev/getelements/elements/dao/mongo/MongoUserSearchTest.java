package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.AppleSignInSessionDao;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

import static dev.getelements.elements.model.user.User.Level.USER;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;

@Guice(modules = IntegrationTestModule.class)
public class MongoUserSearchTest {

    private int TEST_USER_COUNT = 5;

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private AppleSignInSessionDao appleSignInSessionDao;

    private ProfileDao profileDao;

    private Application testApplication;

    private List<User> testUsers;

    private List<Profile> testProfiles;

    @BeforeClass
    public void setup() {
        testApplication = makeTestApplication();
        testUsers = buildTestUsers();
        testProfiles = buildTestProfiles();
    }

    public Application makeTestApplication() {
        final Application application = new Application();
        application.setName("user_search_test");
        application.setDescription("A mock application.");
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    private List<User> buildTestUsers() {

        final var users = new java.util.ArrayList<User>(emptyList());

        for(int i = 0; i < TEST_USER_COUNT; i++){
            final User testUser = new User();
            testUser.setName(format("testysearch.mctesterson.%d", i));
            testUser.setEmail(format("testysearch.mctesterson.%d@example.com", i));
            testUser.setLevel(USER);
            users.add(getUserDao().createOrReactivateUser(testUser));
        }

        return users;

    }

    private List<Profile> buildTestProfiles() {

        final var profiles = new java.util.ArrayList<Profile>(emptyList());

        for(int i = 0; i< TEST_USER_COUNT; i++){
            final var profile =  new Profile();
            profile.setUser(testUsers.get(i));
            profile.setApplication(testApplication);
            profile.setDisplayName(format("testysearch.display-name-%s", testUsers.get(i).getName()));
            profile.setImageUrl(format("http://example.com/%s.png", testUsers.get(i).getName()));
            profiles.add(getProfileDao().createOrReactivateProfile(profile));
        }

        return profiles;

    }

    @Test(enabled = false)
    public void testUserSearch() {
        var users = getUserDao().getActiveUsers(0, 0, "testysearch");
        assertEquals(users.getTotal(), TEST_USER_COUNT);

        for(int i = 0; i < TEST_USER_COUNT; i++){
            users = getUserDao().getActiveUsers(0, 0, format("%d@", i));
            assertEquals(users.getTotal(), 1);
        }
    }

    @Test(enabled = false)
    public void testProfileSearch() {

        var profiles = getProfileDao().getActiveProfiles(0, 0, "testysearch.display");
        assertEquals(profiles.getTotal(), TEST_USER_COUNT);

        for(int i = 0; i < TEST_USER_COUNT; i++){
            profiles = getProfileDao().getActiveProfiles(0, 0, format("%d@", i));
            assertEquals(profiles.getTotal(), 1);
        }

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
