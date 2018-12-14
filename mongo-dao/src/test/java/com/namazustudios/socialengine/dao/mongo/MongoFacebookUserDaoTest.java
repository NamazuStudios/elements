package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.UserNotFoundException;
import com.namazustudios.socialengine.model.User;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFacebookUserDaoTest {

    private UserDao userDao;

    private FacebookUserDao facebookUserDao;

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
    public void testCreateOrRefreshWitnNoExistingUser() {

        final User user = new User();
        user.setLevel(USER);
        user.setActive(true);
        user.setFacebookId("1234567890");
        user.setName("testy.mctestersen.0");
        user.setEmail("testy.mctestersen.0@example.com");

        final User result = getFacebookUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFacebookId(), "1234567890");
        assertTrue(result.isActive());
        assertEquals(result.getName(), "testy.mctestersen.0");
        assertEquals(result.getEmail(), "testy.mctestersen.0@example.com");
        assertEquals(result.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testCreateOrRefreshWitnNoExistingUser")
    public void testCreateOrRefreshWithExistingUser() {
        testCreateOrRefreshWitnNoExistingUser();
    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithExistingUser")
    public void testReactivatesInactiveUser() {

        final User user = getUserDao().getActiveUserByNameOrEmail("testy.mctestersen.0@example.com");
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

        final User user = new User();
        user.setLevel(USER);
        user.setActive(true);
        user.setFacebookId("1234567890");
        user.setName("testy.mctestersen.0");
        user.setEmail("testy.mctestersen.0@gmail.com");

        final User result = getFacebookUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFacebookId(), "1234567890");
        assertTrue(result.isActive());
        assertEquals(result.getEmail(), "testy.mctestersen.0@example.com");
        assertEquals(result.getLevel(), USER);

    }

    @Test
    public void testConnectIfNecessaryUnconnected() {

        final User user = new User();
        user.setLevel(USER);
        user.setActive(true);
        user.setName("testy.mctesterson.1");
        user.setEmail("testy.mctesterson.1@example.com");

        final User inserted = getUserDao().createUserWithPasswordStrict(user, "Testy's Dog Named Fido");

        assertNotNull(inserted.getId());
        assertTrue(ObjectId.isValid(inserted.getId()));

        assertNull(inserted.getFacebookId());
        assertTrue(inserted.isActive());
        assertEquals(inserted.getName(), "testy.mctesterson.1");
        assertEquals(inserted.getEmail(), "testy.mctesterson.1@example.com");
        assertEquals(inserted.getLevel(), USER);

        inserted.setFacebookId("0987654321");
        final User connected = getFacebookUserDao().connectActiveFacebookUserIfNecessary(inserted);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getFacebookId(), "0987654321");
        assertEquals(connected.getName(), "testy.mctesterson.1");
        assertEquals(connected.getEmail(), "testy.mctesterson.1@example.com");
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectIfNecessaryUnconnected")
    public void testConnectingSameUserHasNoSideEffects() {

        final User user = getUserDao().getActiveUserByNameOrEmail("testy.mctesterson.1@example.com");
        final User connected = getFacebookUserDao().connectActiveFacebookUserIfNecessary(user);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getFacebookId(), "0987654321");
        assertEquals(connected.getName(), "testy.mctesterson.1");
        assertEquals(connected.getEmail(), "testy.mctesterson.1@example.com");
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectingSameUserHasNoSideEffects", expectedExceptions = DuplicateException.class)
    public void testConnectingFacebookIdFails() {
        final User user = getUserDao().getActiveUserByNameOrEmail("testy.mctesterson.1@example.com");
        user.setFacebookId("1245");
        getFacebookUserDao().connectActiveFacebookUserIfNecessary(user);
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

}
