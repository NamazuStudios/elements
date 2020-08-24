package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.AppleSignInUserDao;
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
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoAppleSignInUserDaoTest {

    private UserDao userDao;

    private AppleSignInUserDao applappleSignInUserDao;

    @BeforeClass
    public void seedOtherUsers() {
        for (int i = 50; i < 100; ++i) {
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
        user.setAppleSignInId("1234567890");
        user.setName("testy.mctestersen.0");
        user.setEmail("testy.mctestersen.0@example.com");

        final User result = getApplappleSignInUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getAppleSignInId(), "1234567890");
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
        user.setAppleSignInId("1234567890");
        user.setName("testy.mctestersen.0");
        user.setEmail("testy.mctestersen.0@gmail.com");

        final User result = getApplappleSignInUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getAppleSignInId(), "1234567890");
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

        assertNull(inserted.getAppleSignInId());
        assertTrue(inserted.isActive());
        assertEquals(inserted.getName(), "testy.mctesterson.1");
        assertEquals(inserted.getEmail(), "testy.mctesterson.1@example.com");
        assertEquals(inserted.getLevel(), USER);

        inserted.setAppleSignInId("0987654321");
        final User connected = getApplappleSignInUserDao().connectActiveAppleUserIfNecessary(inserted);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getAppleSignInId(), "0987654321");
        assertEquals(connected.getName(), "testy.mctesterson.1");
        assertEquals(connected.getEmail(), "testy.mctesterson.1@example.com");
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectIfNecessaryUnconnected")
    public void testConnectingSameUserHasNoSideEffects() {

        final User user = getUserDao().getActiveUserByNameOrEmail("testy.mctesterson.51@example.com");
        final User connected = getApplappleSignInUserDao().connectActiveAppleUserIfNecessary(user);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getAppleSignInId(), "0987654321");
        assertEquals(connected.getName(), "testy.mctesterson.1");
        assertEquals(connected.getEmail(), "testy.mctesterson.1@example.com");
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectingSameUserHasNoSideEffects", expectedExceptions = DuplicateException.class)
    public void testConnectingAppleSignInIdFails() {
        final User user = getUserDao().getActiveUserByNameOrEmail("testy.mctesterson.1@example.com");
        user.setAppleSignInId("1245");
        getApplappleSignInUserDao().connectActiveAppleUserIfNecessary(user);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public AppleSignInUserDao getApplappleSignInUserDao() {
        return applappleSignInUserDao;
    }

    @Inject
    public void setApplappleSignInUserDao(AppleSignInUserDao applappleSignInUserDao) {
        this.applappleSignInUserDao = applappleSignInUserDao;
    }

}
