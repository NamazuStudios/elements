package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Set;

import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_NAME;
import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.String.format;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

@Guice(modules = IntegrationTestModule.class)
public class MongoUserUidDaoTest {

    private UserUidDao userUidDao;

    private UserDao userDao;

    private static final String TEST_SCHEME_NAME = "MongoTestUserUidScheme";

    private UserTestFactory userTestFactory;

    private User testUserA;

    @BeforeClass
    public void createTestUsers() {
        final var testUser = new User();
        final var userName = "MongoUserUidDaoTestUserA";

        testUser.setName(userName);
        testUser.setEmail(format("%s@example.com", userName));
        testUser.setLevel(USER);
        testUser.setLinkedAccounts(Set.of(
                UserUidDao.SCHEME_EMAIL,
                UserUidDao.SCHEME_NAME
        ));

        testUserA = getUserDao().createUser(testUser);
    }

    @Test
    public void testFindByName() {

        final var result = getUserUidDao().findUserUid(testUserA.getName(), UserUidDao.SCHEME_NAME);

        assertTrue(result.isPresent());

        final var uid = result.get();

        assertEquals(uid.getId(), testUserA.getName());
        assertEquals(uid.getScheme(), UserUidDao.SCHEME_NAME);
        assertEquals(uid.getUserId(), testUserA.getId());
    }

    @Test()
    public void testGetByEmail() {
        final var uid = getUserUidDao().getUserUid(testUserA.getEmail(), SCHEME_EMAIL);

        assertNotNull(uid);

        assertEquals(uid.getId(), testUserA.getEmail());
        assertEquals(uid.getScheme(), SCHEME_EMAIL);
        assertEquals(uid.getUserId(), testUserA.getId());
    }

    @Test(dependsOnMethods = "testGetByEmail")
    public void testCreatingNewUid() {

        final var userUid = new UserUid();
        userUid.setUserId(testUserA.getId());
        userUid.setId(testUserA.getId());
        userUid.setScheme(TEST_SCHEME_NAME);
        final var result = userUidDao.createUserUidStrict(userUid);

        assertEquals(result.getId(), userUid.getId());
        assertEquals(result.getScheme(), userUid.getScheme());
        assertEquals(result.getUserId(), userUid.getUserId());
    }

    @Test(dependsOnMethods = "testCreatingNewUid", expectedExceptions = DuplicateException.class)
    public void testRecreateUid() {

        final var userUid = userUidDao.getUserUid(testUserA.getId(), TEST_SCHEME_NAME);
        userUid.setUserId(testUserA.getId());
        userUid.setId(testUserA.getId());
        userUid.setScheme(TEST_SCHEME_NAME);

        userUidDao.createUserUid(userUid);
    }

    @Test(dependsOnMethods = "testRecreateUid")
    public void testSearchByUserId() {

        final var result = getUserUidDao().getUserUids(0, 20, testUserA.getId());

        assertNotNull(result);
        assertFalse(result.getObjects().isEmpty());
        assertTrue(result.stream().allMatch(uid -> uid.getUserId().equals(testUserA.getId())));
        assertTrue(result.stream().anyMatch(uid -> uid.getScheme().equals(SCHEME_EMAIL)));
        assertTrue(result.stream().anyMatch(uid -> uid.getScheme().equals(SCHEME_NAME)));
        assertTrue(result.stream().anyMatch(uid -> uid.getScheme().equals(TEST_SCHEME_NAME)));
    }

    @Test(dependsOnMethods = "testRecreateUid")
    public void testSearchByScheme() {

        final var result = getUserUidDao().getUserUids(0, 20, SCHEME_EMAIL);

        assertNotNull(result);
        assertFalse(result.getObjects().isEmpty());
        assertTrue(result.stream().anyMatch(uid -> uid.getScheme().equals(SCHEME_EMAIL)));
        assertTrue(result.stream().anyMatch(uid -> uid.getId().equals(testUserA.getEmail())));
    }

    @Test(dependsOnMethods = "testRecreateUid")
    public void testSearchById() {

        final var result = getUserUidDao().getUserUids(0, 20, testUserA.getName());

        assertNotNull(result);
        assertFalse(result.getObjects().isEmpty());
        assertTrue(result.stream().anyMatch(uid -> uid.getScheme().equals(SCHEME_NAME)));
        assertTrue(result.stream().anyMatch(uid -> uid.getId().equals(testUserA.getName())));

    }

    public UserUidDao getUserUidDao() {
        return userUidDao;
    }

    @Inject
    public void setUserUidDao(UserUidDao userUidDao) {
        this.userUidDao = userUidDao;
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
