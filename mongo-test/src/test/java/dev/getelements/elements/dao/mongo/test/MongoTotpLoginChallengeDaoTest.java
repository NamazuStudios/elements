package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.TotpLoginChallengeDao;
import jakarta.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.sql.Timestamp;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoTotpLoginChallengeDaoTest {

    private TotpLoginChallengeDao totpLoginChallengeDao;

    @Test
    public void testCreateAndFindRoundTrips() {

        final var expiry = new Timestamp(System.currentTimeMillis() + 60_000);

        final var id = getTotpLoginChallengeDao().createChallenge(
                "user-1", "profile-1", null, "app-1", expiry
        );

        assertNotNull(id);

        final var found = getTotpLoginChallengeDao().find(id);

        assertTrue(found.isPresent());
        assertEquals(found.get().getUserId(), "user-1");
        assertEquals(found.get().getProfileId(), "profile-1");
        assertNull(found.get().getProfileSelector());
        assertEquals(found.get().getApplicationNameOrId(), "app-1");

    }

    @Test
    public void testFindDoesNotConsume() {

        final var expiry = new Timestamp(System.currentTimeMillis() + 60_000);
        final var id = getTotpLoginChallengeDao().createChallenge("user-2", null, null, null, expiry);

        // A failed verification attempt must be retryable against the same challenge -- find() alone must
        // never remove it, only an explicit consume() should.
        assertTrue(getTotpLoginChallengeDao().find(id).isPresent());
        assertTrue(getTotpLoginChallengeDao().find(id).isPresent());
        assertTrue(getTotpLoginChallengeDao().find(id).isPresent());

    }

    @Test
    public void testConsumeIsSingleUse() {

        final var expiry = new Timestamp(System.currentTimeMillis() + 60_000);
        final var id = getTotpLoginChallengeDao().createChallenge("user-3", null, null, null, expiry);

        assertTrue(getTotpLoginChallengeDao().find(id).isPresent());
        getTotpLoginChallengeDao().consume(id);
        assertTrue(getTotpLoginChallengeDao().find(id).isEmpty());

    }

    @Test
    public void testExpiredChallengeIsTreatedAsAbsent() {

        final var alreadyExpired = new Timestamp(System.currentTimeMillis() - 1_000);
        final var id = getTotpLoginChallengeDao().createChallenge("user-4", null, null, null, alreadyExpired);

        assertTrue(getTotpLoginChallengeDao().find(id).isEmpty());

    }

    @Test
    public void testUnknownChallengeIsAbsent() {
        assertTrue(getTotpLoginChallengeDao().find("does-not-exist").isEmpty());
    }

    @Test
    public void testConsumingUnknownChallengeDoesNotThrow() {
        getTotpLoginChallengeDao().consume("does-not-exist");
    }

    public TotpLoginChallengeDao getTotpLoginChallengeDao() {
        return totpLoginChallengeDao;
    }

    @Inject
    public void setTotpLoginChallengeDao(TotpLoginChallengeDao totpLoginChallengeDao) {
        this.totpLoginChallengeDao = totpLoginChallengeDao;
    }

}
