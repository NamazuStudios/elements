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
    public void testCreateAndConsumeRoundTrips() {

        final var expiry = new Timestamp(System.currentTimeMillis() + 60_000);

        final var id = getTotpLoginChallengeDao().createChallenge(
                "user-1", "profile-1", null, "app-1", expiry
        );

        assertNotNull(id);

        final var consumed = getTotpLoginChallengeDao().findAndConsume(id);

        assertTrue(consumed.isPresent());
        assertEquals(consumed.get().getUserId(), "user-1");
        assertEquals(consumed.get().getProfileId(), "profile-1");
        assertNull(consumed.get().getProfileSelector());
        assertEquals(consumed.get().getApplicationNameOrId(), "app-1");

    }

    @Test
    public void testConsumeIsSingleUse() {

        final var expiry = new Timestamp(System.currentTimeMillis() + 60_000);
        final var id = getTotpLoginChallengeDao().createChallenge("user-2", null, null, null, expiry);

        assertTrue(getTotpLoginChallengeDao().findAndConsume(id).isPresent());
        assertTrue(getTotpLoginChallengeDao().findAndConsume(id).isEmpty());

    }

    @Test
    public void testExpiredChallengeIsTreatedAsAbsent() {

        final var alreadyExpired = new Timestamp(System.currentTimeMillis() - 1_000);
        final var id = getTotpLoginChallengeDao().createChallenge("user-3", null, null, null, alreadyExpired);

        assertTrue(getTotpLoginChallengeDao().findAndConsume(id).isEmpty());

    }

    @Test
    public void testUnknownChallengeIsAbsent() {
        assertTrue(getTotpLoginChallengeDao().findAndConsume("does-not-exist").isEmpty());
    }

    public TotpLoginChallengeDao getTotpLoginChallengeDao() {
        return totpLoginChallengeDao;
    }

    @Inject
    public void setTotpLoginChallengeDao(TotpLoginChallengeDao totpLoginChallengeDao) {
        this.totpLoginChallengeDao = totpLoginChallengeDao;
    }

}
