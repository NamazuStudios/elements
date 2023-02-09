package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.model.user.User;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFacebookUserDaoTestConnecting {

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
    public void testConnectIfNecessaryUnconnected() {

        final var user = getUserTestFactory().createTestUser();
        final var userName = user.getName();
        final var email = user.getEmail();

        final var inserted = getUserDao().createUserWithPasswordStrict(user, "Testy's Dog Named Fido");

        assertNotNull(inserted.getId());
        assertTrue(ObjectId.isValid(inserted.getId()));

        assertNull(inserted.getFacebookId());
        assertTrue(inserted.isActive());
        assertEquals(inserted.getName(), userName);
        assertEquals(inserted.getEmail(), email);
        assertEquals(inserted.getLevel(), USER);

        final var facebookId = "0987654321";
        inserted.setFacebookId(facebookId);

        final var connected = getFacebookUserDao().connectActiveUserIfNecessary(inserted);

        assertNotNull(connected.getId());
        assertTrue(ObjectId.isValid(connected.getId()));

        assertTrue(connected.isActive());
        assertEquals(connected.getFacebookId(), facebookId);
        assertEquals(inserted.getName(), userName);
        assertEquals(inserted.getEmail(), email);
        assertEquals(connected.getLevel(), USER);

        currentUser = connected;
    }

    @Test(dependsOnMethods = "testConnectIfNecessaryUnconnected")
    public void testConnectingSameUserHasNoSideEffects() {

        final var user = getUserDao().getActiveUserByNameOrEmail(currentUser.getEmail());
        final var connected = getFacebookUserDao().connectActiveUserIfNecessary(user);

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
