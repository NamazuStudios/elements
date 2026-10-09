package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import org.testng.annotations.Test;

import java.util.List;

import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.CLEAN;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.FAILED;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.UNSTABLE;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.WARNINGS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Covers the mission critical portion of the health check. Only deployments flagged as mission critical can make the
 * instance unhealthy, and only when the runtime failed to load them entirely or completed with errors.
 */
public class StandardDeploymentHealthStatusDaoTest {

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

    private static StandardDeploymentHealthStatusDao daoWith(final List<RuntimeRecord> runtimes) {
        final var elementRuntimeService = mock(ElementRuntimeService.class);
        when(elementRuntimeService.getActiveRuntimes()).thenReturn(runtimes);

        final var dao = new StandardDeploymentHealthStatusDao();
        dao.setElementRuntimeService(elementRuntimeService);

        return dao;
    }

}
