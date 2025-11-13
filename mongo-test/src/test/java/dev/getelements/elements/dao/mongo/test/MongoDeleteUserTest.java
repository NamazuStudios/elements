package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

@Guice(modules = IntegrationTestModule.class)
public class MongoDeleteUserTest {

    private User user;

    private List<Profile> profiles = new ArrayList<>();

    private Application application;

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private ProfileDao profileDao;

    private UserUidDao userUidDao;

    private final String name = "testuser-name-" + randomUUID().toString();

    private final String email = "testuser-email-" + randomUUID().toString() + "@example.com";

    private final String password = randomUUID().toString();

    private final String primaryPhoneNumber = "12345678901";

    @DataProvider
    public Object[][] userIdProvider() {
        return new Object[][] {
                new Object[]{name, UserUidDao.SCHEME_NAME},
                new Object[]{email, UserUidDao.SCHEME_EMAIL},
                new Object[]{primaryPhoneNumber, UserUidDao.SCHEME_PHONE_NUMBER},
        };
    }

    @Test void createApplication() {

        final var toCreate = new Application();
        toCreate.setName("MongoDeleteUserTestApplication");

        application = applicationDao.createOrUpdateInactiveApplication(toCreate);
    }

    @Test(dependsOnMethods = "createApplication")
    public void createUser() {

        final var toCreate = new User();
        toCreate.setName(name);
        toCreate.setPrimaryPhoneNb(primaryPhoneNumber);
        toCreate.setEmail(email);
        toCreate.setLevel(User.Level.USER);
        toCreate.setFirstName("Test");
        toCreate.setLastName("User");

        user = userDao.createUserWithPassword(toCreate, password);

        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getEmail());
        assertEquals(user.getLevel(), User.Level.USER);
    }

    @Test(dependsOnMethods = "createUser", dataProvider = "userIdProvider")
    public void createProfiles(final String userId, final String scheme) {

        final var toCreate = new Profile();

        toCreate.setUser(user);
        toCreate.setDisplayName(scheme);
        toCreate.setApplication(application);

        final var metadata = new HashMap<String, Object>();
        metadata.put("foo", "bar");

        toCreate.setMetadata(metadata);

        final var profile = profileDao.createOrReactivateProfile(toCreate);

        assertNotNull(profile.getMetadata());
        assertNotNull(profile.getId());
        assertEquals(profile.getUser(), user);
        assertEquals(profile.getApplication(), application);

        profiles.add(profile);
    }

    @Test(dependsOnMethods = "createProfiles")
    public void deleteUser() {

        userDao.softDeleteUser(user.getId());

        final var u = userDao.findUser(user.getId());

        //User is present so as not to break connections, but is fully scrubbed
        assertTrue(u.isPresent());
        assertNull(u.get().getName());
        assertNull(u.get().getFirstName());
        assertNull(u.get().getLastName());
        assertNull(u.get().getPrimaryPhoneNb());
        assertNull(u.get().getEmail());
    }

    @Test(dependsOnMethods = "deleteUser", dataProvider = "userIdProvider")
    public void verifyUserUIDDeletion(final String userId, final String scheme) {

        //UserUID is deleted
        final var userUID = userUidDao.findUserUid(userId, scheme);

        assertFalse(userUID.isPresent());
    }

    @Test(dependsOnMethods = "deleteUser")
    public void verifyProfileDeletion() {

        for (final Profile profile : profiles) {

            //Profile is in DB but flagged as inactive
            final var p = profileDao.findActiveProfile(profile.getId());

            assertFalse(p.isPresent());
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

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
    }
}

