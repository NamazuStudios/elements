package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.LongSupplier;

import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.CLEAN;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.FAILED;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.UNSTABLE;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.WARNINGS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Covers the mission critical portion of the health check. Only deployments flagged as mission critical can make the
 * instance unhealthy, and only when the runtime failed to load them entirely, completed with errors, or -- since
 * #209 -- when the deployment is enabled in the database but has no live runtime on this instance beyond the absence
 * threshold (the frozen-reconcile / stuck-in-backoff blind spot).
 */
public class StandardDeploymentHealthStatusDaoTest {

    private ElementRuntimeService elementRuntimeServiceMock;

    private ElementDeploymentDao elementDeploymentDaoMock;

    @Test
    public void reportsNothingWhenNoDeploymentsAreActive() {
        assertTrue(daoWith(List.of()).getUnhealthyMissionCriticalDeployments().isEmpty());
    }

    @Test
    public void ignoresHealthyMissionCriticalDeployments() {
        final var dao = daoWith(List.of(
                runtime(CLEAN, true, "core-payments", "deployment-clean"),
                runtime(WARNINGS, true, "core-payments", "deployment-warnings")
        ));

        assertTrue(dao.getUnhealthyMissionCriticalDeployments().isEmpty(),
                "CLEAN and WARNINGS deployments should not be reported as unhealthy");
    }

    @Test
    public void ignoresUnhealthyDeploymentsWhichAreNotMissionCritical() {
        final var dao = daoWith(List.of(
                runtime(FAILED, false, "core-payments", "deployment-failed"),
                runtime(UNSTABLE, false, "core-payments", "deployment-unstable")
        ));

        assertTrue(dao.getUnhealthyMissionCriticalDeployments().isEmpty(),
                "deployments which are not mission critical should never affect health");
    }

    @Test
    public void reportsFailedMissionCriticalDeployments() {
        final var dao = daoWith(List.of(runtime(FAILED, true, "core-payments", "deployment-failed")));

        assertEquals(dao.getUnhealthyMissionCriticalDeployments(), List.of("core-payments (deployment-failed)"));
    }

    @Test
    public void reportsUnstableMissionCriticalDeployments() {
        final var dao = daoWith(List.of(runtime(UNSTABLE, true, "core-payments", "deployment-unstable")));

        assertEquals(dao.getUnhealthyMissionCriticalDeployments(), List.of("core-payments (deployment-unstable)"));
    }

    @Test
    public void reportsEachUnhealthyMissionCriticalDeployment() {
        final var dao = daoWith(List.of(
                runtime(FAILED, true, "core-payments", "deployment-failed"),
                runtime(CLEAN, true, "core-payments", "deployment-clean"),
                runtime(UNSTABLE, true, "core-payments", "deployment-unstable"),
                runtime(FAILED, false, "core-payments", "deployment-not-critical")
        ));

        assertEquals(dao.getUnhealthyMissionCriticalDeployments(), List.of(
                "core-payments (deployment-failed)",
                "core-payments (deployment-unstable)"
        ));
    }

    @Test
    public void identifiesUnnamedDeploymentsById() {
        final var dao = daoWith(List.of(runtime(FAILED, true, null, "deployment-failed")));

        assertEquals(dao.getUnhealthyMissionCriticalDeployments(), List.of("deployment-failed"));
    }

    /**
     * The #209 blind spot: a pod whose reconcile froze mid-reload (or whose load is stuck in retry backoff)
     * has NO runtime record for an enabled, mission-critical deployment. The old logic only inspected active
     * runtimes, so such a pod reported a perfectly healthy instance while its element routes were dead --
     * and stayed in the load balancer's rotation. A deployment missing beyond the absence threshold must be
     * reported.
     */
    @Test
    public void reportsEnabledMissionCriticalDeploymentsMissingBeyondTheAbsenceThreshold() {

        final var dao = daoWith(List.of()); // no runtimes at all: the frozen-pod case
        final var clock = new MutableClock();
        dao.setTicker(clock);
        dao.setAbsenceThresholdSeconds(600);

        givenEnabledDeployment(deployment(true, "core-payments", "deployment-missing"));

        // First observation: the absence clock starts; not yet reported.
        clock.set(0L);
        assertTrue(dao.getUnhealthyMissionCriticalDeployments().isEmpty(),
                "a deployment missing within the grace window must not be reported "
                        + "(a normal hot reload also removes the record for the load duration)");

        // Well past the threshold: reported.
        clock.set(601L * 1_000_000_000L);
        assertEquals(dao.getUnhealthyMissionCriticalDeployments(), List.of("core-payments (deployment-missing)"),
                "an enabled mission-critical deployment missing beyond the grace window must be unhealthy");

    }

    /**
     * A deployment which reappears (the reload completed, or the pod caught up) must leave the absence
     * tracker: a later absence starts a fresh grace window rather than reporting immediately.
     */
    @Test
    public void reappearanceResetsTheAbsenceWindow() {

        final var dao = daoWith(List.of());
        final var clock = new MutableClock();
        dao.setTicker(clock);
        dao.setAbsenceThresholdSeconds(600);

        givenEnabledDeployment(deployment(true, "core-payments", "deployment-missing"));

        clock.set(500L * 1_000_000_000L);  // missing 500s: within grace
        dao.getUnhealthyMissionCriticalDeployments();

        // The deployment loads...
        final var cleanRuntime = runtime(CLEAN, true, "core-payments", "deployment-missing");
        when(elementRuntimeServiceMock.getActiveRuntimes()).thenReturn(List.of(cleanRuntime));
        assertTrue(dao.getUnhealthyMissionCriticalDeployments().isEmpty());

        // ...and vanishes again: the grace window must start over, not inherit the old absence.
        when(elementRuntimeServiceMock.getActiveRuntimes()).thenReturn(List.of());
        clock.set(560L * 1_000_000_000L);  // only 60s after reappearance
        assertTrue(dao.getUnhealthyMissionCriticalDeployments().isEmpty(),
                "a reappeared deployment must get a fresh absence window on a later disappearance");

    }

    @Test
    public void ignoresMissingDeploymentsWhichAreNotMissionCritical() {

        final var dao = daoWith(List.of());
        final var clock = new MutableClock();
        dao.setTicker(clock);
        dao.setAbsenceThresholdSeconds(600);

        givenEnabledDeployment(deployment(false, "dev-playground", "deployment-missing"));

        clock.set(3_600L * 1_000_000_000L);  // an hour: far past the threshold
        assertTrue(dao.getUnhealthyMissionCriticalDeployments().isEmpty(),
                "deployments which are not mission critical should never affect health");

    }

    @Test
    public void failedReadOfEnabledDeploymentsReportsNothingMissing() {

        final var dao = daoWith(List.of());
        final var clock = new MutableClock();
        dao.setTicker(clock);

        when(elementDeploymentDaoMock.getElementDeploymentsByState(any()))
                .thenThrow(new RuntimeException("mongo is having a moment"));

        clock.set(3_600L * 1_000_000_000L);
        assertTrue(dao.getUnhealthyMissionCriticalDeployments().isEmpty(),
                "without a data source the missing-deployment check must report nothing rather than guess");

    }

    private void givenEnabledDeployment(final ElementDeployment deployment) {
        when(elementDeploymentDaoMock.getElementDeploymentsByState(ElementDeploymentState.ENABLED))
                .thenReturn(List.of(deployment));
    }

    private static ElementDeployment deployment(final boolean missionCritical,
                                                final String name,
                                                final String id) {
        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(id);
        when(deployment.name()).thenReturn(name);
        when(deployment.missionCritical()).thenReturn(missionCritical);
        return deployment;
    }

    /** A settable nanosecond clock for deterministic absence-window tests. */
    private static class MutableClock implements LongSupplier {

        private long nanos;

        void set(final long nanos) {
            this.nanos = nanos;
        }

        @Override
        public long getAsLong() {
            return nanos;
        }

    }

    private static RuntimeRecord runtime(final RuntimeStatus status,
                                         final boolean missionCritical,
                                         final String name,
                                         final String id) {

        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(id);
        when(deployment.name()).thenReturn(name);
        when(deployment.missionCritical()).thenReturn(missionCritical);

        final var record = mock(RuntimeRecord.class);
        when(record.deployment()).thenReturn(deployment);
        when(record.status()).thenReturn(status);

        return record;
    }

    private StandardDeploymentHealthStatusDao daoWith(final List<RuntimeRecord> runtimes) {
        elementRuntimeServiceMock = mock(ElementRuntimeService.class);
        when(elementRuntimeServiceMock.getActiveRuntimes()).thenReturn(runtimes);

        elementDeploymentDaoMock = mock(ElementDeploymentDao.class);
        when(elementDeploymentDaoMock.getElementDeploymentsByState(any())).thenReturn(List.of());

        final var dao = new StandardDeploymentHealthStatusDao();
        dao.setElementRuntimeService(elementRuntimeServiceMock);
        dao.setElementDeploymentDao(elementDeploymentDaoMock);

        return dao;
    }

}
