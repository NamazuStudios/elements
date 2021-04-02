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
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoAppleSignInUserDaoTest {

    private UserDao userDao;

    private AppleSignInUserDao applappleSignInUserDao;

    private static final String TEST_APPLE_SIGNIN_ID_0 = "000425.f984e387f8704c48bd634900b356c615.0240";

    private static final String TEST_APPLE_SIGNIN_ID_1 = "000425.f984e387f8704c48bd634900b356c615.0241";

    private static final String TEST_BOGUS_APPLE_SIGNIN_ID = "000425.f984e387f8704c48bd634900b356c615.0242";

    private UserTestFactory userTestFactory;

    private User testUserA;
    private User testUserB;

    @BeforeClass
    public void createTestUsers() {
        //Seeds random users
        for (int i = 0; i < 50; ++i) {
            userTestFactory.createTestUser();
        }

        testUserA = userTestFactory.createTestUser(false);
        testUserB = userTestFactory.createTestUser();
    }

    @Test
    public void testCreateOrRefreshWithNoExistingUser() {

        testUserA.setAppleSignInId(TEST_APPLE_SIGNIN_ID_0);
        final User result = getApplappleSignInUserDao().createReactivateOrUpdateUser(testUserA);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getAppleSignInId(), TEST_APPLE_SIGNIN_ID_0);
        assertTrue(result.isActive());
        assertEquals(result.getName(), testUserA.getName());
        assertEquals(result.getEmail(), testUserA.getEmail());
        assertEquals(result.getLevel(), USER);
    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithNoExistingUser")
    public void testCreateOrRefreshWithExistingUser() {
        testCreateOrRefreshWithNoExistingUser();
    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithExistingUser")
    public void testReactivatesInactiveUser() {

        final User user = getUserDao().getActiveUserByNameOrEmail(testUserA.getEmail());
        getUserDao().softDeleteUser(user.getId());

        try {
            getUserDao().getActiveUserByNameOrEmail(testUserA.getEmail());
        } catch (UserNotFoundException expected) {
            // Expected exception.  Continue test.
            testCreateOrRefreshWithExistingUser();
            return;
        }

        fail("Did not hit expected exception.");

    }

// TODO: Fix this test before merging SOC-367 -- PHT
//    @Test(dependsOnMethods = "testReactivatesInactiveUser")
//    public void testUserChangedEmailAddress() {
//
//        final User user = new User();
//        user.setLevel(USER);
//        user.setActive(true);
//        user.setAppleSignInId(TEST_APPLE_SIGNIN_ID_0);
//        user.setName(TEST_USER_0);
//        user.setEmail(TEST_EMAIL_ALTERNATE_0);
//
//        final User result = getApplappleSignInUserDao().createReactivateOrUpdateUser(user);
//
//        assertNotNull(result.getId());
//        assertTrue(ObjectId.isValid(result.getId()));
//
//        assertEquals(result.getAppleSignInId(), TEST_APPLE_SIGNIN_ID_0);
//        assertTrue(result.isActive());
//        assertEquals(result.getEmail(), TEST_EMAIL_ALTERNATE_0);
//        assertEquals(result.getLevel(), USER);
//
//    }

    @Test
    public void testConnectIfNecessaryUnconnected() {

        final User inserted = getUserDao().createUserWithPasswordStrict(testUserB, "Testy's Dog Named Fido");

        assertNotNull(inserted.getId());
        assertTrue(ObjectId.isValid(inserted.getId()));

        assertNull(inserted.getAppleSignInId());
        assertTrue(inserted.isActive());
        assertEquals(inserted.getName(), testUserB.getName());
        assertEquals(inserted.getEmail(), testUserB.getEmail());
        assertEquals(inserted.getLevel(), USER);

        inserted.setAppleSignInId(TEST_APPLE_SIGNIN_ID_1);
        final User connected = getApplappleSignInUserDao().connectActiveUserIfNecessary(inserted);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getAppleSignInId(), TEST_APPLE_SIGNIN_ID_1);
        assertEquals(connected.getName(), testUserB.getName());
        assertEquals(connected.getEmail(), testUserB.getEmail());
        assertEquals(connected.getLevel(), USER);

        testUserB = connected;
    }

    @Test(dependsOnMethods = "testConnectIfNecessaryUnconnected")
    public void testConnectingSameUserHasNoSideEffects() {

        final User user = getUserDao().getActiveUserByNameOrEmail(testUserB.getEmail());
        final User connected = getApplappleSignInUserDao().connectActiveUserIfNecessary(user);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getAppleSignInId(), TEST_APPLE_SIGNIN_ID_1);
        assertEquals(connected.getName(), testUserB.getName());
        assertEquals(connected.getEmail(), testUserB.getEmail());
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectingSameUserHasNoSideEffects", expectedExceptions = DuplicateException.class)
    public void testConnectingAppleSignInIdFails() {
        final User user = getUserDao().getActiveUserByNameOrEmail(testUserB.getEmail());
        user.setAppleSignInId(TEST_BOGUS_APPLE_SIGNIN_ID);
        getApplappleSignInUserDao().connectActiveUserIfNecessary(user);
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

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
