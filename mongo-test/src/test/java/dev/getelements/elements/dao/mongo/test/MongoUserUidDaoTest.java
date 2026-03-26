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

    @Test(dependsOnMethods = "testRecreateUid")
    public void testGetAll() {

        final var result = getUserUidDao().getAllUserIdsForUser(testUserA);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(uid -> uid.getScheme().equals(SCHEME_NAME)));
        assertTrue(result.stream().anyMatch(uid -> uid.getId().equals(testUserA.getName())));

    }


    @Test(dependsOnMethods = "testRecreateUid")
    public void testLinkedAccounts() {
        final var result = getUserDao().getUser(testUserA.getId());
        assertTrue(result.getLinkedAccounts().contains(SCHEME_NAME));
        assertTrue(result.getLinkedAccounts().contains(SCHEME_EMAIL));
        assertTrue(result.getLinkedAccounts().contains(TEST_SCHEME_NAME));
    }

    @Test(dependsOnMethods = "testLinkedAccounts")
    public void testDeleteScheme() {

        getUserUidDao().deleteUserUid(TEST_SCHEME_NAME, testUserA.getId());

        final var result = getUserDao().getUser(testUserA.getId());
        assertTrue(result.getLinkedAccounts().contains(SCHEME_NAME));
        assertTrue(result.getLinkedAccounts().contains(SCHEME_EMAIL));
        assertFalse(result.getLinkedAccounts().contains(TEST_SCHEME_NAME));

    }

    /**
     * Verifies that {@link UserUidDao#getUserUid(String, String)} correctly distinguishes two different
     * users that share the same scheme. If the compound-ID Morphia filter is broken (e.g. ignoring the
     * id field), both lookups would return the same document and this test would fail.
     */
    @Test
    public void testCompoundIdLookupDistinguishesTwoUsers() {

        final String MULTI_USER_SCHEME = "CompoundIdTestScheme";

        final var userB = new User();
        final var userBName = "MongoUserUidDaoTestUserB_CompoundId";
        userB.setName(userBName);
        userB.setEmail(format("%s@example.com", userBName));
        userB.setLevel(USER);
        final var createdUserB = getUserDao().createUser(userB);

        final var userC = new User();
        final var userCName = "MongoUserUidDaoTestUserC_CompoundId";
        userC.setName(userCName);
        userC.setEmail(format("%s@example.com", userCName));
        userC.setLevel(USER);
        final var createdUserC = getUserDao().createUser(userC);

        final var uidB = new UserUid();
        uidB.setUserId(createdUserB.getId());
        uidB.setId("external_bob");
        uidB.setScheme(MULTI_USER_SCHEME);
        getUserUidDao().createUserUid(uidB);

        final var uidC = new UserUid();
        uidC.setUserId(createdUserC.getId());
        uidC.setId("external_carol");
        uidC.setScheme(MULTI_USER_SCHEME);
        getUserUidDao().createUserUid(uidC);

        final var foundB = getUserUidDao().getUserUid("external_bob", MULTI_USER_SCHEME);
        assertEquals(foundB.getUserId(), createdUserB.getId());
        assertNotEquals(foundB.getUserId(), createdUserC.getId());

        final var foundC = getUserUidDao().getUserUid("external_carol", MULTI_USER_SCHEME);
        assertEquals(foundC.getUserId(), createdUserC.getId());
        assertNotEquals(foundC.getUserId(), createdUserB.getId());
    }

    @Test(dependsOnMethods = "testDeleteScheme")
    public void testSoftDeleteUser() {

        getUserUidDao().softDeleteUser(testUserA);

        final var user = getUserDao().getUser(testUserA.getId());
        assertNull(user.getLinkedAccounts());

        final var allUids = getUserUidDao().getAllUserIdsForUser(testUserA);
        assertTrue(allUids.isEmpty());

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
