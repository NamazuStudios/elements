package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.user.User;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFacebookUserDaoTest {

    private UserDao userDao;

    private FacebookUserDao facebookUserDao;

    private UserTestFactory userTestFactory;

    private User currentUser;

    @BeforeClass
    public void seedOtherUsers() {
        for (int i = 0; i < 50; ++i) {
            final User user = new User();
            user.setLevel(USER);
            user.setActive(true);
            user.setName(format("test%s", randomUUID()));
            user.setEmail(format("test%s@example.com", randomUUID()));
            getUserDao().createUserStrict(user);
        }
    }

    @Test
    public void testCreateOrRefreshWithNoExistingUser() {

        final String facebookId = "1234567890";

        currentUser = getUserTestFactory().createTestUser(user -> user.setFacebookId(facebookId));
        final String userName = currentUser.getName();
        final String email = currentUser.getEmail();

        currentUser.setFacebookId(facebookId);

        final User result = getFacebookUserDao().createReactivateOrUpdateUser(currentUser);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFacebookId(), facebookId);
        assertTrue(result.isActive());
        assertEquals(result.getName(), userName);
        assertEquals(result.getEmail(), email);
        assertEquals(result.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithNoExistingUser")
    public void testCreateOrRefreshWithExistingUser() {
        testCreateOrRefreshWithNoExistingUser();
    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithExistingUser")
    public void testReactivatesInactiveUser() {

        final User user = getUserDao().getActiveUserByNameOrEmail(currentUser.getEmail());
        getUserDao().softDeleteUser(user.getId());

        try {
            getUserDao().getActiveUserByNameOrEmail("testy.mctestersen.0@example.com");
        } catch (UserNotFoundException expected) {
            // Expected exception.  Continue test.
            testCreateOrRefreshWithExistingUser();
            return;
        }

        fail("Did not hit expected exception.");

    }

    @Test(dependsOnMethods = "testReactivatesInactiveUser")
    public void testUserChangedEmailAddress() {

        final String facebookId = "1234567890";
        final User user = getUserTestFactory().createTestUser(u -> u.setFacebookId(facebookId));
        final String userName = user.getName();
        final String email = user.getEmail();

        final User result = getFacebookUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFacebookId(), facebookId);
        assertTrue(result.isActive());
        assertEquals(result.getEmail(), email);
        assertEquals(result.getLevel(), USER);

    }

    @Test
    public void testConnectIfNecessaryUnconnected() {

        final User user = getUserTestFactory().createTestUser();
        final String userName = user.getName();
        final String email = user.getEmail();

        final User inserted = getUserDao().createUserWithPasswordStrict(user, "Testy's Dog Named Fido");

        assertNotNull(inserted.getId());
        assertTrue(ObjectId.isValid(inserted.getId()));

        assertNull(inserted.getFacebookId());
        assertTrue(inserted.isActive());
        assertEquals(inserted.getName(), userName);
        assertEquals(inserted.getEmail(), email);
        assertEquals(inserted.getLevel(), USER);

        final String facebookId = "0987654321";
        inserted.setFacebookId(facebookId);

        final User connected = getFacebookUserDao().connectActiveUserIfNecessary(inserted);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getFacebookId(), facebookId);
        assertEquals(inserted.getName(), userName);
        assertEquals(inserted.getEmail(), email);
        assertEquals(connected.getLevel(), USER);

        currentUser = user;
    }

    @Test(dependsOnMethods = "testConnectIfNecessaryUnconnected")
    public void testConnectingSameUserHasNoSideEffects() {

        final User user = getUserDao().getActiveUserByNameOrEmail(currentUser.getEmail());
        final User connected = getFacebookUserDao().connectActiveUserIfNecessary(user);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getFacebookId(), currentUser.getFacebookId());
        assertEquals(connected.getName(), currentUser.getName());
        assertEquals(connected.getEmail(), currentUser.getEmail());
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectingSameUserHasNoSideEffects", expectedExceptions = DuplicateException.class)
    public void testConnectingFacebookIdFails() {
        final User user = getUserDao().getActiveUserByNameOrEmail(currentUser.getEmail());
        user.setFacebookId("1245");
        getFacebookUserDao().connectActiveUserIfNecessary(user);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public FacebookUserDao getFacebookUserDao() {
        return facebookUserDao;
    }

    @Inject
    public void setFacebookUserDao(FacebookUserDao facebookUserDao) {
        this.facebookUserDao = facebookUserDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
