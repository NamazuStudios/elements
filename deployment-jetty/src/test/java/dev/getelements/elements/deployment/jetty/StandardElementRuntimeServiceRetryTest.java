package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Regression coverage for #147: a deployment that fails to load latches {@link
 * ElementRuntimeService.RuntimeStatus#FAILED} and was never retried, so a transient failure (a network blip, a
 * momentarily unreachable artifact repository, a Mongo timeout) permanently bricked the deployment until an
 * operator PUT the deployment again or the process restarted.
 * <p>
 * These tests drive {@link StandardElementRuntimeService#reconcile()} directly, using a deployment that fails to
 * load without touching the network (no ELM, no elements, no packages). Each load attempt is observed by the
 * number of subordinate registries requested from the root registry, which is the first thing
 * {@code doLoadDeployment} does on every attempt.
 */
public class StandardElementRuntimeServiceRetryTest {

    private static final long INITIAL_DELAY_NANOS = TimeUnit.SECONDS.toNanos(30);

    private ElementDeploymentDao dao;

    private MutableElementRegistry rootRegistry;

    private AtomicLong clock;

    private AtomicInteger loadAttempts;

    @Test
    public void failedDeploymentIsRetriedAfterBackoff() {

        final var service = newService();
        givenDeployments(failingDeployment("deployment-1", 1L));

        service.reconcile();
        assertEquals(loadAttempts.get(), 1, "first reconcile must attempt a load");

        // Before the backoff elapses, no retry.
        service.reconcile();
        assertEquals(loadAttempts.get(), 1, "must not retry before the initial backoff elapses");

        // After the initial backoff elapses, the FAILED deployment is retried.
        clock.set(INITIAL_DELAY_NANOS + 1);
        service.reconcile();
        assertEquals(loadAttempts.get(), 2, "FAILED deployment must be retried once the backoff elapses");

    }

    @Test
    public void retryBackoffGrowsPerConsecutiveFailure() {

        final var service = newService();
        givenDeployments(failingDeployment("deployment-1", 1L));

        service.reconcile();
        assertEquals(loadAttempts.get(), 1);

        // First retry permitted after 30s.
        clock.set(TimeUnit.SECONDS.toNanos(31));
        service.reconcile();
        assertEquals(loadAttempts.get(), 2);

        // Second retry needs 60s from the second attempt (t=31s => not before t=91s).
        clock.set(TimeUnit.SECONDS.toNanos(62));
        service.reconcile();
        assertEquals(loadAttempts.get(), 2, "retry must wait for the grown backoff");

        clock.set(TimeUnit.SECONDS.toNanos(91));
        service.reconcile();
        assertEquals(loadAttempts.get(), 3, "retry must occur once the grown backoff elapses");

    }

    @Test
    public void versionBumpReloadsAndResetsRetryBackoff() {

        final var service = newService();

        givenDeployments(failingDeployment("deployment-1", 1L));
        service.reconcile();
        assertEquals(loadAttempts.get(), 1);

        // Push the retry state into the "grown backoff" regime.
        clock.set(TimeUnit.SECONDS.toNanos(31));
        service.reconcile();
        assertEquals(loadAttempts.get(), 2);

        // Bumping the version must reload immediately and discard the accumulated backoff...
        givenDeployments(failingDeployment("deployment-1", 2L));
        clock.set(TimeUnit.SECONDS.toNanos(32));
        service.reconcile();
        assertEquals(loadAttempts.get(), 3, "a version bump must reload immediately");

        // ...so the last failure (at t=32s) is retried after the initial 30s delay (t=62s), not after the
        // pre-bump accumulated backoff, which would have demanded t=91s. The reset backoff still gates the retry.
        service.reconcile();
        assertEquals(loadAttempts.get(), 3, "the reset backoff must still gate the retry");

        clock.set(TimeUnit.SECONDS.toNanos(62));
        service.reconcile();
        assertEquals(loadAttempts.get(), 4, "retry state must be reset to the initial delay by a version bump");

    }

    @Test
    public void unloadClearsRetryState() {

        final var service = newService();

        givenDeployments(failingDeployment("deployment-1", 1L));
        service.reconcile();
        assertEquals(loadAttempts.get(), 1);

        // Deployment removed from the database - reconciliation unloads it.
        givenDeployments();
        service.reconcile();
        assertEquals(loadAttempts.get(), 1);

        // Re-added: active is absent, so it loads again as new.
        givenDeployments(failingDeployment("deployment-1", 1L));
        service.reconcile();
        assertEquals(loadAttempts.get(), 2);

    }

    @Test
    public void retryDelayDoublesAndCaps() {

        assertEquals(StandardElementRuntimeService.retryDelayNanos(1), TimeUnit.SECONDS.toNanos(30));
        assertEquals(StandardElementRuntimeService.retryDelayNanos(2), TimeUnit.SECONDS.toNanos(60));
        assertEquals(StandardElementRuntimeService.retryDelayNanos(3), TimeUnit.SECONDS.toNanos(120));
        assertEquals(StandardElementRuntimeService.retryDelayNanos(4), TimeUnit.SECONDS.toNanos(240));
        assertEquals(StandardElementRuntimeService.retryDelayNanos(5), TimeUnit.SECONDS.toNanos(480));
        assertEquals(StandardElementRuntimeService.retryDelayNanos(6), TimeUnit.SECONDS.toNanos(600));
        assertEquals(StandardElementRuntimeService.retryDelayNanos(100), TimeUnit.SECONDS.toNanos(600));

    }

    private StandardElementRuntimeService newService() {

        loadAttempts = new AtomicInteger();
        clock = new AtomicLong();

        rootRegistry = mock(MutableElementRegistry.class);
        when(rootRegistry.newSubordinateRegistry()).thenAnswer(invocation -> {
            loadAttempts.incrementAndGet();
            return mock(MutableElementRegistry.class);
        });

        dao = mock(ElementDeploymentDao.class);

        final var service = new StandardElementRuntimeService(mock(ElementArtifactLoader.class));
        service.setElementDeploymentDao(dao);
        service.setRootElementRegistry(rootRegistry);
        service.setSystemAttributes(Attributes.emptyAttributes());
        service.setPollIntervalSeconds(1);
        service.setTicker(clock::get);

        return service;

    }

    private void givenDeployments(final ElementDeployment... deployments) {
        when(dao.getElementDeploymentsByState(eq(ElementDeploymentState.ENABLED)))
                .thenReturn(List.of(deployments));
    }

    /**
     * A deployment that fails to load locally, without touching the network: it has no ELM, no element
     * definitions, and no packages, so staging produces no element paths and {@code loadElements} throws.
     */
    private static ElementDeployment failingDeployment(final String id, final long version) {
        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(id);
        when(deployment.version()).thenReturn(version);
        when(deployment.useDefaultRepositories()).thenReturn(false);
        when(deployment.repositories()).thenReturn(List.of());
        when(deployment.elm()).thenReturn(null);
        return deployment;
    }

}
