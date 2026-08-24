package dev.getelements.elements.dao.mongo.test;

import com.mongodb.MongoException;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.OidcLoginAttemptDao;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttempt;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttemptStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.dao.OidcLoginAttemptDao.*;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoOidcLoginAttemptDaoTest {

    @Inject
    private OidcLoginAttemptDao oidcLoginAttemptDao;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    private final List<OidcLoginAttempt> createdEvents = new CopyOnWriteArrayList<>();

    private final List<OidcLoginAttempt> updatedEvents = new CopyOnWriteArrayList<>();

    @BeforeClass
    public void setupEventHandlers() {
        elementRegistry.onEvent(ev -> {
            switch (ev.getEventName()) {
                case OIDC_LOGIN_ATTEMPT_CREATED -> createdEvents.add(ev.getEventArgument(0, OidcLoginAttempt.class));
                case OIDC_LOGIN_ATTEMPT_UPDATED -> updatedEvents.add(ev.getEventArgument(0, OidcLoginAttempt.class));
            }
        });
    }

    private OidcLoginAttempt newAttempt(final String provider) {

        final var attempt = new OidcLoginAttempt();
        attempt.setId(UUID.randomUUID().toString());
        attempt.setProvider(provider);
        attempt.setState(UUID.randomUUID().toString());
        attempt.setNonce(UUID.randomUUID().toString());
        attempt.setStatus(OidcLoginAttemptStatus.PENDING);
        attempt.setExpiry(new Timestamp(System.currentTimeMillis() + 300_000));

        final var created = getOidcLoginAttemptDao().create(attempt);

        assertTrue(
                createdEvents.stream().anyMatch(e -> e.getId().equals(created.getId())),
                "Expected OIDC_LOGIN_ATTEMPT_CREATED event for " + created.getId()
        );

        return created;

    }

    @Test
    public void testCreateAndFindPendingByState() {

        final var created = newAttempt("twitch");

        final var found = getOidcLoginAttemptDao().findPendingByState("twitch", created.getState());
        assertTrue(found.isPresent());
        assertEquals(found.get().getId(), created.getId());

        // Provider mismatch must not find the attempt, even with a correct state.
        assertTrue(getOidcLoginAttemptDao().findPendingByState("google", created.getState()).isEmpty());

    }

    @Test(expectedExceptions = MongoException.class)
    public void testDuplicateStateRejected() {

        final var first = newAttempt("twitch");

        final var second = new OidcLoginAttempt();
        second.setId(UUID.randomUUID().toString());
        second.setProvider("twitch");
        second.setState(first.getState());
        second.setNonce(UUID.randomUUID().toString());
        second.setStatus(OidcLoginAttemptStatus.PENDING);
        second.setExpiry(new Timestamp(System.currentTimeMillis() + 300_000));

        getOidcLoginAttemptDao().create(second);

    }

    @Test
    public void testMarkCompleteThenReplayFailsClosed() {

        final var created = newAttempt("twitch");

        final var completed = getOidcLoginAttemptDao().markComplete(created.getState(), "{\"sessionSecret\":\"abc\"}");
        assertTrue(completed.isPresent());
        assertEquals(completed.get().getStatus(), OidcLoginAttemptStatus.COMPLETE);

        assertTrue(
                updatedEvents.stream().anyMatch(e ->
                        e.getId().equals(created.getId()) && e.getStatus() == OidcLoginAttemptStatus.COMPLETE),
                "Expected OIDC_LOGIN_ATTEMPT_UPDATED event for completed attempt " + created.getId()
        );

        // A second, replayed callback for the same state must not succeed again.
        final var replayed = getOidcLoginAttemptDao().markComplete(created.getState(), "{\"sessionSecret\":\"xyz\"}");
        assertTrue(replayed.isEmpty());

        // And must not clobber the originally stored session.
        final var claimed = getOidcLoginAttemptDao().claimCompleteById(created.getId());
        assertTrue(claimed.isPresent());
        assertEquals(claimed.get().getSessionToken(), "{\"sessionSecret\":\"abc\"}");

        assertTrue(
                updatedEvents.stream().anyMatch(e ->
                        e.getId().equals(created.getId()) && "{\"sessionSecret\":\"abc\"}".equals(e.getSessionToken())),
                "Expected OIDC_LOGIN_ATTEMPT_UPDATED event for claimed attempt " + created.getId()
        );

    }

    @Test
    public void testMarkFailedGuardedByPendingStatus() {

        final var created = newAttempt("twitch");

        assertTrue(getOidcLoginAttemptDao().markComplete(created.getState(), "{}").isPresent());

        // Cannot fail an attempt that already resolved to COMPLETE.
        final var failed = getOidcLoginAttemptDao().markFailed(created.getState(), "too late");
        assertTrue(failed.isEmpty());

    }

    @Test
    public void testClaimCompleteByIdIsSingleUse() {

        final var created = newAttempt("twitch");
        getOidcLoginAttemptDao().markComplete(created.getState(), "{\"sessionSecret\":\"once\"}");

        final var firstClaim = getOidcLoginAttemptDao().claimCompleteById(created.getId());
        assertTrue(firstClaim.isPresent());
        assertEquals(firstClaim.get().getSessionToken(), "{\"sessionSecret\":\"once\"}");

        final var secondClaim = getOidcLoginAttemptDao().claimCompleteById(created.getId());
        assertTrue(secondClaim.isEmpty(), "COMPLETE must be readable exactly once");

        // Once claimed, it must also not be reported as PENDING or FAILED.
        assertTrue(getOidcLoginAttemptDao().findPendingOrFailedById(created.getId()).isEmpty());

    }

    @Test
    public void testConcurrentClaimHasExactlyOneWinner() throws Exception {

        final var created = newAttempt("twitch");
        getOidcLoginAttemptDao().markComplete(created.getState(), "{\"sessionSecret\":\"race\"}");

        final int threadCount = 8;
        final ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        try {

            final List<Callable<Boolean>> tasks = IntStream.range(0, threadCount)
                    .mapToObj(i -> (Callable<Boolean>) () ->
                            getOidcLoginAttemptDao().claimCompleteById(created.getId()).isPresent())
                    .collect(Collectors.toList());

            final List<Future<Boolean>> futures = pool.invokeAll(tasks);

            long winners = 0;

            for (final var future : futures) {
                if (future.get()) {
                    winners++;
                }
            }

            assertEquals(winners, 1, "Exactly one concurrent claim should win");

        } finally {
            pool.shutdown();
        }

    }

    @Test
    public void testFindPendingOrFailedByIdReportsFailure() {

        final var created = newAttempt("twitch");
        getOidcLoginAttemptDao().markFailed(created.getState(), "denied");

        final var found = getOidcLoginAttemptDao().findPendingOrFailedById(created.getId());
        assertTrue(found.isPresent());
        assertEquals(found.get().getStatus(), OidcLoginAttemptStatus.FAILED);
        assertEquals(found.get().getFailureReason(), "denied");

        assertTrue(
                updatedEvents.stream().anyMatch(e ->
                        e.getId().equals(created.getId()) && e.getStatus() == OidcLoginAttemptStatus.FAILED),
                "Expected OIDC_LOGIN_ATTEMPT_UPDATED event for failed attempt " + created.getId()
        );

    }

    @Test
    public void testExpiredAttemptIsNotReturned() {

        final var attempt = new OidcLoginAttempt();
        attempt.setId(UUID.randomUUID().toString());
        attempt.setProvider("twitch");
        attempt.setState(UUID.randomUUID().toString());
        attempt.setNonce(UUID.randomUUID().toString());
        attempt.setStatus(OidcLoginAttemptStatus.PENDING);
        // Already expired — the defensive read-time check should reject it even before the TTL sweep runs.
        attempt.setExpiry(new Timestamp(System.currentTimeMillis() - 60_000));

        final var created = getOidcLoginAttemptDao().create(attempt);

        assertTrue(getOidcLoginAttemptDao().findPendingByState("twitch", created.getState()).isEmpty());
        assertTrue(getOidcLoginAttemptDao().findPendingOrFailedById(created.getId()).isEmpty());

    }

    public OidcLoginAttemptDao getOidcLoginAttemptDao() {
        return oidcLoginAttemptDao;
    }

}
