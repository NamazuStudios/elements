package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.FirebaseUserDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.model.user.User;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static dev.getelements.elements.model.user.User.Level.USER;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFirebaseUserDaoTestConnecting {

    private static final String FIREBASE_ID = "4CaxZ9JhjbZpKv9vsPCQL1I6Kyk1";

    private static final String BOGUS_FIREBASE_ID = "G3xPvlZPTXUTeh2CD9R4DpPH7Ny2";

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
    public void testConnectIfNecessaryUnconnected() {

        final User user = getUserTestFactory().createTestUser();
        final String userName = user.getName();
        final String email = user.getEmail();

        final User inserted = getUserDao().createUserWithPasswordStrict(user, "Testy's Dog Named Fido");

        assertNotNull(inserted.getId());
        assertTrue(ObjectId.isValid(inserted.getId()));

        assertNull(inserted.getFirebaseId());
        assertTrue(inserted.isActive());
        assertEquals(inserted.getName(), userName);
        assertEquals(inserted.getEmail(), email);
        assertEquals(inserted.getLevel(), USER);

        inserted.setFirebaseId(FIREBASE_ID);

        final User connected = getFirebaseUserDao().connectActiveUserIfNecessary(inserted);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getFirebaseId(), FIREBASE_ID);
        assertEquals(inserted.getName(), userName);
        assertEquals(inserted.getEmail(), email);
        assertEquals(connected.getLevel(), USER);

        currentUser = connected;

    }

    @Test(dependsOnMethods = "testConnectIfNecessaryUnconnected")
    public void testConnectingSameUserHasNoSideEffects() {

        final User user = getUserDao().getActiveUserByNameOrEmail(currentUser.getEmail());
        final User connected = getFirebaseUserDao().connectActiveUserIfNecessary(user);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getFirebaseId(), currentUser.getFirebaseId());
        assertEquals(connected.getName(), currentUser.getName());
        assertEquals(connected.getEmail(), currentUser.getEmail());
        assertEquals(connected.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testConnectingSameUserHasNoSideEffects", expectedExceptions = DuplicateException.class)
    public void testConnectingFirebaseIdFails() {
        final User user = getUserDao().getActiveUserByNameOrEmail(currentUser.getEmail());
        user.setFirebaseId(BOGUS_FIREBASE_ID);
        getFirebaseUserDao().connectActiveUserIfNecessary(user);
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
