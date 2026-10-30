package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao.SUPERUSER_INVITE_CREATED;
import static dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao.SUPERUSER_INVITE_REDEEMED;
import static dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao.SUPERUSER_INVITE_REVOKED;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration coverage for {@link MongoSuperUserInviteTokenDao}: persistence of the invite record,
 * state transitions (pending → redeemed / revoked), expiry filtering, email normalization, and the
 * DAO-level events. Expiry-based cleanup itself relies on a MongoDB TTL index and is only checked
 * logically (an expired token is treated as absent); physical TTL deletion is not asserted.
 */
@Guice(modules = IntegrationTestModule.class)
public class MongoSuperUserInviteTokenDaoTest {

    private static final String EMAIL = "SuperUserInviteDaoTest@example.com";

    @Inject
    private SuperUserInviteTokenDao superUserInviteTokenDao;

    @Inject
    private UserTestFactory userTestFactory;

    private User issuer;

    private long hourFromNow;

    private long hourAgo;

    @BeforeClass
    public void createIssuer() {
        issuer = userTestFactory.createTestUser();
        hourFromNow = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        hourAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
    }

    @Test
    public void createInvite_persistsPendingRecord() {

        final var expiry = new Timestamp(hourFromNow);
        final var createdAt = new Timestamp(System.currentTimeMillis());

        final var token = superUserInviteTokenDao.createInvite(issuer, EMAIL, expiry, createdAt);

        assertNotNull(token);
        assertFalse(token.isBlank());

        final var invite = superUserInviteTokenDao.findTokenAnyState(token).orElseThrow();

        assertEquals(invite.getToken(), token);
        assertEquals(invite.getInvitedEmail(), EMAIL.toLowerCase());
        assertEquals(invite.getInvitedBy().getId(), issuer.getId());
        assertEquals(invite.getStatus(), SuperUserInvite.Status.PENDING);
        assertNotNull(invite.getCreatedAt());
        assertNotNull(invite.getExpiry());
        assertNull(invite.getRedeemedAt());
        assertNull(invite.getRevokedAt());
    }

    @Test
    public void createInvite_normalizesEmail() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, "  MiXeD@ExAmPlE.CoM  ", new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        final var invite = superUserInviteTokenDao.findTokenAnyState(token).orElseThrow();
        assertEquals(invite.getInvitedEmail(), "mixed@example.com");
    }

    @Test
    public void eventNames_stableContract() {
        // The DAO publishes under these names; service/element consumers bind to them, so they are
        // part of the contract and must not drift silently.
        assertEquals(SUPERUSER_INVITE_CREATED, "dev.getelements.elements.sdk.dao.superuser.invite.created");
        assertEquals(SUPERUSER_INVITE_REDEEMED, "dev.getelements.elements.sdk.dao.superuser.invite.redeemed");
        assertEquals(SUPERUSER_INVITE_REVOKED, "dev.getelements.elements.sdk.dao.superuser.invite.revoked");
    }

    @Test
    public void findToken_presentForPending() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        final var found = superUserInviteTokenDao.findToken(token);

        assertTrue(found.isPresent());
        assertEquals(found.get().getStatus(), SuperUserInvite.Status.PENDING);
    }

    @Test
    public void findToken_absentForUnknown() {
        assertTrue(superUserInviteTokenDao.findToken("no-such-token").isEmpty());
        assertTrue(superUserInviteTokenDao.findToken(null).isEmpty());
    }

    @Test
    public void findToken_absentForExpired() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourAgo), new Timestamp(hourAgo));

        assertTrue(superUserInviteTokenDao.findToken(token).isEmpty());
        assertTrue(superUserInviteTokenDao.findTokenAnyState(token).isPresent());
    }

    @Test
    public void findToken_absentAfterRedeemed() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        assertTrue(superUserInviteTokenDao.redeemToken(token, new Timestamp(System.currentTimeMillis())));

        assertTrue(superUserInviteTokenDao.findToken(token).isEmpty());
        assertEquals(superUserInviteTokenDao.findTokenAnyState(token).orElseThrow().getStatus(),
                SuperUserInvite.Status.REDEEMED);
    }

    @Test
    public void findToken_absentAfterRevoked() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        assertTrue(superUserInviteTokenDao.revokeToken(token, new Timestamp(System.currentTimeMillis())));

        assertTrue(superUserInviteTokenDao.findToken(token).isEmpty());
        assertEquals(superUserInviteTokenDao.findTokenAnyState(token).orElseThrow().getStatus(),
                SuperUserInvite.Status.REVOKED);
    }

    @Test
    public void findTokenAnyState_absentForUnknown() {
        assertTrue(superUserInviteTokenDao.findTokenAnyState("no-such-token").isEmpty());
    }

    @Test
    public void findPendingInviteByEmail_findsPendingUnexpired() {

        final var email = format("pending-%d@example.com", System.nanoTime());

        final var token = superUserInviteTokenDao.createInvite(
                issuer, email, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        final var found = superUserInviteTokenDao.findPendingInviteByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(found.get().getToken(), token);
    }

    @Test
    public void findPendingInviteByEmail_caseInsensitive() {

        final var email = format("caseinsensitive-%d@example.com", System.nanoTime());

        superUserInviteTokenDao.createInvite(
                issuer, email, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        assertTrue(superUserInviteTokenDao.findPendingInviteByEmail(email.toUpperCase()).isPresent());
    }

    @Test
    public void findPendingInviteByEmail_ignoresRedeemedRevokedExpired() {

        final var base = System.nanoTime();

        final var redeemedToken = superUserInviteTokenDao.createInvite(
                issuer, format("state-a-%d@example.com", base), new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));
        superUserInviteTokenDao.redeemToken(redeemedToken, new Timestamp(System.currentTimeMillis()));

        final var revokedToken = superUserInviteTokenDao.createInvite(
                issuer, format("state-b-%d@example.com", base), new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));
        superUserInviteTokenDao.revokeToken(revokedToken, new Timestamp(System.currentTimeMillis()));

        superUserInviteTokenDao.createInvite(
                issuer, format("state-c-%d@example.com", base), new Timestamp(hourAgo), new Timestamp(hourAgo));

        assertTrue(superUserInviteTokenDao.findPendingInviteByEmail(format("state-a-%d@example.com", base)).isEmpty());
        assertTrue(superUserInviteTokenDao.findPendingInviteByEmail(format("state-b-%d@example.com", base)).isEmpty());
        assertTrue(superUserInviteTokenDao.findPendingInviteByEmail(format("state-c-%d@example.com", base)).isEmpty());
    }

    @Test
    public void findPendingInviteByEmail_returnsNewestFirst() {

        final var email = format("newest-%d@example.com", System.nanoTime());

        superUserInviteTokenDao.createInvite(
                issuer, email, new Timestamp(hourFromNow), new Timestamp(hourAgo));

        // Ensure a strictly newer createdAt (MongoDB timestamp granularity is milliseconds).
        try { Thread.sleep(5); } catch (final InterruptedException ignored) {}

        final var newest = superUserInviteTokenDao.createInvite(
                issuer, email, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        final var found = superUserInviteTokenDao.findPendingInviteByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(found.get().getToken(), newest);
        assertNotEquals(found.get().getToken(), "");
    }

    @Test
    public void findPendingInviteByEmail_emptyForNullOrBlank() {
        assertTrue(superUserInviteTokenDao.findPendingInviteByEmail(null).isEmpty());
        assertTrue(superUserInviteTokenDao.findPendingInviteByEmail("  ").isEmpty());
    }

    @Test
    public void getInvites_paginatesNewestFirst() {

        final var email = format("paged-%d@example.com", System.nanoTime());

        final var first = superUserInviteTokenDao.createInvite(
                issuer, email, new Timestamp(hourFromNow), new Timestamp(hourAgo));

        try { Thread.sleep(5); } catch (final InterruptedException ignored) {}

        final var second = superUserInviteTokenDao.createInvite(
                issuer, email, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        final var page = superUserInviteTokenDao.getInvites(0, 1);

        assertNotNull(page);
        assertEquals(page.getOffset(), 0);
        assertTrue(page.getTotal() >= 2);
        assertEquals(page.getObjects().size(), 1);
        // Newest first.
        assertEquals(page.getObjects().get(0).getToken(), second);
        assertNotEquals(page.getObjects().get(0).getToken(), first);
    }

    @Test
    public void redeemToken_singleUse() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        final var redeemedAt = new Timestamp(System.currentTimeMillis());

        assertTrue(superUserInviteTokenDao.redeemToken(token, redeemedAt));
        assertFalse(superUserInviteTokenDao.redeemToken(token, redeemedAt), "Second redemption must fail.");

        final var invite = superUserInviteTokenDao.findTokenAnyState(token).orElseThrow();
        assertEquals(invite.getStatus(), SuperUserInvite.Status.REDEEMED);
        assertNotNull(invite.getRedeemedAt());
    }

    /**
     * The pending → redeemed/revoked transition must be an atomic status-guarded find-and-modify. A
     * read-then-merge here let two concurrent redemptions of the same token both succeed (double
     * elevation), and let an in-flight redemption overwrite a concurrent revocation. Exactly one
     * contender may win.
     */
    @Test
    public void redeemToken_concurrentRedemptionsExactlyOneWins() throws Exception {

        final var rounds = 8;

        for (var round = 0; round < rounds; round++) {

            final var token = superUserInviteTokenDao.createInvite(
                    issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

            final var contenders = 8;
            final var startLatch = new java.util.concurrent.CountDownLatch(1);
            final var pool = java.util.concurrent.Executors.newFixedThreadPool(contenders);
            final var successes = new java.util.concurrent.atomic.AtomicInteger();

            final var futures = new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
            for (var i = 0; i < contenders; i++) {
                futures.add(pool.submit(() -> {
                    startLatch.await();
                    return superUserInviteTokenDao.redeemToken(
                            token, new Timestamp(System.currentTimeMillis()));
                }));
            }

            startLatch.countDown();

            for (final var future : futures) {
                if (future.get()) successes.incrementAndGet();
            }

            pool.shutdown();
            assertTrue(pool.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS));

            assertEquals(successes.get(), 1,
                    "Exactly one concurrent redemption may win (round " + round + ").");

            final var invite = superUserInviteTokenDao.findTokenAnyState(token).orElseThrow();
            assertEquals(invite.getStatus(), SuperUserInvite.Status.REDEEMED);
        }

    }

    /**
     * A revocation must not be overwritable by an in-flight redemption: once revoked, a redeem must
     * observe the status change and fail, never resurrect the invite as REDEEMED.
     */
    @Test
    public void redeemToken_cannotOverwriteAConcurrentRevocation() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        assertTrue(superUserInviteTokenDao.revokeToken(token, new Timestamp(System.currentTimeMillis())));
        assertFalse(superUserInviteTokenDao.redeemToken(token, new Timestamp(System.currentTimeMillis())),
                "A revoked invitation must not be redeemable.");

        final var invite = superUserInviteTokenDao.findTokenAnyState(token).orElseThrow();
        assertEquals(invite.getStatus(), SuperUserInvite.Status.REVOKED);
        assertNotNull(invite.getRevokedAt());
        assertNull(invite.getRedeemedAt());

    }

    @Test
    public void redeemToken_failsForUnknownExpiredOrNonPending() {
        assertFalse(superUserInviteTokenDao.redeemToken("no-such-token", new Timestamp(System.currentTimeMillis())));

        final var expired = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourAgo), new Timestamp(hourAgo));
        assertFalse(superUserInviteTokenDao.redeemToken(expired, new Timestamp(System.currentTimeMillis())),
                "Expired invites must not be redeemable.");
    }

    @Test
    public void revokeToken_onlyForPending() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        assertTrue(superUserInviteTokenDao.revokeToken(token, new Timestamp(System.currentTimeMillis())));
        assertFalse(superUserInviteTokenDao.revokeToken(token, new Timestamp(System.currentTimeMillis())),
                "Revoking twice must fail.");

        final var invite = superUserInviteTokenDao.findTokenAnyState(token).orElseThrow();
        assertEquals(invite.getStatus(), SuperUserInvite.Status.REVOKED);
        assertNotNull(invite.getRevokedAt());
    }

    @Test
    public void revokeToken_failsForUnknown() {
        assertFalse(superUserInviteTokenDao.revokeToken("no-such-token", new Timestamp(System.currentTimeMillis())));
    }

    @Test
    public void revokeToken_failsAfterRedeemed() {

        final var token = superUserInviteTokenDao.createInvite(
                issuer, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        assertTrue(superUserInviteTokenDao.redeemToken(token, new Timestamp(System.currentTimeMillis())));
        assertFalse(superUserInviteTokenDao.revokeToken(token, new Timestamp(System.currentTimeMillis())),
                "A redeemed invite must not be revocable.");
    }

    @Test
    public void createInvite_nullIssuer_persistsWithoutIssuer() {

        final var token = superUserInviteTokenDao.createInvite(
                null, EMAIL, new Timestamp(hourFromNow), new Timestamp(System.currentTimeMillis()));

        final var invite = superUserInviteTokenDao.findTokenAnyState(token).orElseThrow();
        assertNull(invite.getInvitedBy());
    }

}
