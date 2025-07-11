package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.user.User;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.String.format;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoOidcUserDaoTest {

    private UserDao userDao;

    private UserUidDao userUidDao;

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

//        testUserA.setExternalUserId(TEST_APPLE_SIGNIN_ID_0);
        //TODO
//        final User result = getUserDao().createReactivateOrUpdateUser(testUserA);
        final User result = getUserDao().createUser(testUserA);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

//        assertEquals(result.getExternalUserId(), TEST_APPLE_SIGNIN_ID_0);
        assertEquals(result.getName(), testUserA.getName());
        assertEquals(result.getEmail(), testUserA.getEmail());
        assertEquals(result.getLevel(), USER);
    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithNoExistingUser")
    public void testCreateOrRefreshWithExistingUser() {
        testCreateOrRefreshWithNoExistingUser();
    }


// TODO: Fix this test before merging SOC-367 -- PHT
//    @Test(dependsOnMethods = "testReactivatesInactiveUser")
//    public void testUserChangedEmailAddress() {
//
//        final User user = new User();
//        user.setLevel(USER);
//        user.setId(TEST_APPLE_SIGNIN_ID_0);
//        user.setName(TEST_USER_0);
//        user.setEmail(TEST_EMAIL_ALTERNATE_0);
//
//        final User result = getUserDao().createReactivateOrUpdateUser(user);
//
//        assertNotNull(result.getId());
//        assertTrue(ObjectId.isValid(result.getId()));
//
//        assertEquals(result.getId(), TEST_APPLE_SIGNIN_ID_0);
//        assertTrue(result.isActive());
//        assertEquals(result.getEmail(), TEST_EMAIL_ALTERNATE_0);
//        assertEquals(result.getLevel(), USER);
//
//    }

    @Test
    public void testConnectIfNecessaryUnconnected() {

        final User inserted = getUserDao().createUserWithPassword(testUserB, "Testy's Dog Named Fido");

        assertNotNull(inserted.getId());
        assertTrue(ObjectId.isValid(inserted.getId()));

        assertEquals(inserted.getName(), testUserB.getName());
        assertEquals(inserted.getEmail(), testUserB.getEmail());
        assertEquals(inserted.getLevel(), USER);

//        inserted.setExternalUserId(TEST_APPLE_SIGNIN_ID_1);
        //TODO
//        final User connected = getUserDao().connectActiveUserIfNecessary(inserted);
        final User connected = getUserDao().createUser(inserted);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

//        assertEquals(connected.getExternalUserId(), TEST_APPLE_SIGNIN_ID_1);
        assertEquals(connected.getName(), testUserB.getName());
        assertEquals(connected.getEmail(), testUserB.getEmail());
        assertEquals(connected.getLevel(), USER);

        testUserB = connected;
    }

    @Test(dependsOnMethods = "testConnectIfNecessaryUnconnected")
    public void testConnectingSameUserHasNoSideEffects() {

        final User user = getUserDao().getUserByNameOrEmail(testUserB.getEmail());
        final User connected = getUserDao().createUser(user);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertEquals(connected.getName(), testUserB.getName());
        assertEquals(connected.getEmail(), testUserB.getEmail());
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectingSameUserHasNoSideEffects")//TODO restore this when connecting users works: , expectedExceptions = DuplicateException.class)
    public void testConnectingIdFails() {
        final User user = getUserDao().getUserByNameOrEmail(testUserB.getEmail());
        user.setId(TEST_BOGUS_APPLE_SIGNIN_ID);
        getUserDao().createUser(user);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
