package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.FirebaseUserDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.user.UserNotFoundException;
import dev.getelements.elements.model.user.User;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static dev.getelements.elements.model.user.User.Level.USER;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFirebaseUserDaoTestCreating {

    private static final String FIREBASE_ID = "4qDthPW6aPe8cKJa5hAi8eNIZ9d2";

    private UserDao userDao;

    private FirebaseUserDao firebaseUserDao;

    private UserTestFactory userTestFactory;

    private User currentUser;

    @BeforeClass
    public void seedOtherUsers() {
        for (int i = 0; i < 50; ++i) {
            getUserTestFactory().createTestUser();
        }
    }

    @Test
    public void testCreateOrRefreshWithNoExistingUser() {

        final var user = getUserTestFactory().createTestUser(u -> u.setFirebaseId(FIREBASE_ID), false);
        currentUser = getFirebaseUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(currentUser.getId());
        assertTrue(ObjectId.isValid(currentUser.getId()));

        assertEquals(currentUser.getFirebaseId(), FIREBASE_ID);
        assertTrue(currentUser.isActive());
        assertEquals(currentUser.getName(), user.getName());
        assertEquals(currentUser.getEmail(), user.getEmail());
        assertEquals(currentUser.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithNoExistingUser")
    public void testCreateOrRefreshWithExistingUser() {

        final var result = getFirebaseUserDao().createReactivateOrUpdateUser(currentUser);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFirebaseId(), FIREBASE_ID);
        assertTrue(result.isActive());
        assertEquals(result.getId(), currentUser.getId());
        assertEquals(result.getName(), currentUser.getName());
        assertEquals(result.getEmail(), currentUser.getEmail());
        assertEquals(result.getLevel(), USER);

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

        final var user = getUserTestFactory().createTestUser(u -> u.setFirebaseId(FIREBASE_ID), false);

        final var result = getFirebaseUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFirebaseId(), FIREBASE_ID);
        assertTrue(result.isActive());
        assertEquals(result.getEmail(), currentUser.getEmail());
        assertEquals(result.getLevel(), USER);

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public FirebaseUserDao getFirebaseUserDao() {
        return firebaseUserDao;
    }

    @Inject
    public void setFirebaseUserDao(FirebaseUserDao firebaseUserDao) {
        this.firebaseUserDao = firebaseUserDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

}
