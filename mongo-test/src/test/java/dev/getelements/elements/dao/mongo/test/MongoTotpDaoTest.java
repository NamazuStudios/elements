package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;

import static dev.getelements.elements.sdk.model.user.User.Level.USER;
import static java.lang.String.format;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoTotpDaoTest {

    private TotpDao totpDao;

    private UserDao userDao;

    private User testUser;

    @BeforeClass
    public void createTestUser() {
        final var user = new User();
        final var userName = "MongoTotpDaoTestUser";
        user.setName(userName);
        user.setEmail(format("%s@example.com", userName));
        user.setLevel(USER);
        testUser = getUserDao().createUser(user);
    }

    @Test(groups = "find-empty")
    public void testFindStateEmptyWhenNeverEnrolled() {
        assertTrue(getTotpDao().findState(testUser.getId()).isEmpty());
    }

    @Test(groups = "begin", dependsOnGroups = "find-empty")
    public void testBeginEnrollmentStoresPendingSecret() {

        final var state = getTotpDao().beginEnrollment(testUser.getId(), "JBSWY3DPEHPK3PXP");

        assertEquals(state.getSecret(), "JBSWY3DPEHPK3PXP");
        assertFalse(state.isEnabled());

        final var found = getTotpDao().findState(testUser.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().isEnabled());

    }

    @Test(groups = "confirm", dependsOnGroups = "begin")
    public void testConfirmEnrollmentActivatesAndStoresRecoveryCodes() {

        final var state = getTotpDao().confirmEnrollment(testUser.getId(), List.of("hash-1", "hash-2"));

        assertTrue(state.isEnabled());
        assertEquals(state.getRecoveryCodeHashes(), List.of("hash-1", "hash-2"));

        final var found = getTotpDao().findState(testUser.getId());
        assertTrue(found.get().isEnabled());

    }

    @Test(groups = "recovery", dependsOnGroups = "confirm")
    public void testConsumeRecoveryCodeSucceedsOnceThenFails() {

        assertTrue(getTotpDao().consumeRecoveryCode(testUser.getId(), "hash-1"));

        // Consuming the same code twice must fail -- it's single-use.
        assertFalse(getTotpDao().consumeRecoveryCode(testUser.getId(), "hash-1"));

        // The other, unconsumed code must remain usable.
        assertTrue(getTotpDao().consumeRecoveryCode(testUser.getId(), "hash-2"));

    }

    @Test(groups = "disable", dependsOnGroups = "recovery")
    public void testDisableClearsEnrollment() {

        getTotpDao().disable(testUser.getId());

        // Disabling clears the secret entirely, so a subsequent enrollment starts fresh -- findState goes
        // back to reporting empty, exactly as if enrollment had never begun.
        assertTrue(getTotpDao().findState(testUser.getId()).isEmpty());

    }

    public TotpDao getTotpDao() {
        return totpDao;
    }

    @Inject
    public void setTotpDao(TotpDao totpDao) {
        this.totpDao = totpDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}
