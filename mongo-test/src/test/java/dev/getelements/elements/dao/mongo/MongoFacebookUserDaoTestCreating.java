package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.FacebookUserDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.user.UserNotFoundException;
import dev.getelements.elements.model.user.User;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static dev.getelements.elements.model.user.User.Level.USER;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFacebookUserDaoTestCreating {

    private static final String FACEBOOK_ID = "1234567890";

    private UserDao userDao;

    private FacebookUserDao facebookUserDao;

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

        final var user = getUserTestFactory().createTestUser(u -> u.setFacebookId(FACEBOOK_ID), false);
        currentUser = getFacebookUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(currentUser.getId());
        assertTrue(ObjectId.isValid(currentUser.getId()));

        assertEquals(currentUser.getFacebookId(), FACEBOOK_ID);
        assertTrue(currentUser.isActive());
        assertEquals(currentUser.getName(), user.getName());
        assertEquals(currentUser.getEmail(), user.getEmail());
        assertEquals(currentUser.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithNoExistingUser")
    public void testCreateOrRefreshWithExistingUser() {

        final var result = getFacebookUserDao().createReactivateOrUpdateUser(currentUser);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFacebookId(), FACEBOOK_ID);
        assertTrue(result.isActive());
        assertEquals(result.getId(), currentUser.getId());
        assertEquals(result.getName(), currentUser.getName());
        assertEquals(result.getEmail(), currentUser.getEmail());
        assertEquals(result.getLevel(), USER);

    }

    @Test(dependsOnMethods = "testCreateOrRefreshWithExistingUser")
    public void testReactivatesInactiveUser() {

        final var user = getUserDao().getActiveUserByNameOrEmail(currentUser.getEmail());
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

        final var user = getUserTestFactory().createTestUser(u -> u.setFacebookId(FACEBOOK_ID), false);

        final var result = getFacebookUserDao().createReactivateOrUpdateUser(user);

        assertNotNull(result.getId());
        assertTrue(ObjectId.isValid(result.getId()));

        assertEquals(result.getFacebookId(), FACEBOOK_ID);
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
