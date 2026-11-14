package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Regression coverage for #209 (rc-8 staging, 2026-11-14): on a hot reload, the {@code RUNTIME_UNLOADED}
 * event must retire the OLD generation's mounted container immediately -- its elements were already closed
 * by the reload, so leaving it mounted means requests route to handlers serving from closed classloaders
 * for the entire new-version load window (which staging measured in minutes). The mounted container is
 * retained only when it <i>is</i> the runtime service's current generation: a live runtime record at the
 * <b>same version</b> as the mounted container (the async event-reordering case where the fresh mount
 * already replaced the old container).
 */
public class JettyElementContainerServiceStillActiveTest {

    @Test
    public void staleVersionWhileNewVersionLoadsMustNotBeRetained() {
        // The rc-8 signature: the mounted container is the old generation (v91); the runtime service has
        // already begun serving the new version (v92). The old container must retire now, not at remount.
        final var record = runtimeRecord("deployment-1", 92L, ElementRuntimeService.RuntimeStatus.CLEAN);
        assertFalse(JettyElementContainerService.shouldRetainMountedContainer(List.of(record), "deployment-1", 91L),
                "a mounted container at a stale version must retire at unload time, not at remount");
    }

    @Test
    public void noActiveRuntimeMustNotBeRetained() {
        // Deployment deleted, or the unload event processed before the new version loads.
        assertFalse(JettyElementContainerService.shouldRetainMountedContainer(emptyList(), "deployment-1", 91L));
    }

    @Test
    public void sameVersionLiveRuntimeIsRetained() {
        // Event reordering: the fresh mount already replaced the old container before the unload event was
        // processed. The mounted container IS the current generation and must be kept.
        final var record = runtimeRecord("deployment-1", 92L, ElementRuntimeService.RuntimeStatus.CLEAN);
        assertTrue(JettyElementContainerService.shouldRetainMountedContainer(List.of(record), "deployment-1", 92L),
                "the freshly-mounted current generation must survive its own late unload event");
    }

    @Test
    public void failedRuntimeIsNotRetained() {
        // On a failed reload the old container's elements were already closed: the stale container must be
        // unmounted rather than kept serving.
        final var record = runtimeRecord("deployment-1", 92L, ElementRuntimeService.RuntimeStatus.FAILED);
        assertFalse(JettyElementContainerService.shouldRetainMountedContainer(List.of(record), "deployment-1", 92L),
                "a FAILED runtime must not count as still active: the old container must be unmounted");
    }

    @Test
    public void failedRuntimeAtMountedVersionIsNotRetained() {
        final var record = runtimeRecord("deployment-1", 91L, ElementRuntimeService.RuntimeStatus.FAILED);
        assertFalse(JettyElementContainerService.shouldRetainMountedContainer(List.of(record), "deployment-1", 91L));
    }

    @Test
    public void otherDeploymentsDoNotMatch() {
        final var record = runtimeRecord("deployment-2", 91L, ElementRuntimeService.RuntimeStatus.CLEAN);
        assertFalse(JettyElementContainerService.shouldRetainMountedContainer(List.of(record), "deployment-1", 91L));
    }

    private static RuntimeRecord runtimeRecord(final String deploymentId,
                                               final long version,
                                               final ElementRuntimeService.RuntimeStatus status) {
        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(deploymentId);
        when(deployment.version()).thenReturn(version);
        final var record = mock(RuntimeRecord.class);
        when(record.deployment()).thenReturn(deployment);
        when(record.status()).thenReturn(status);
        return record;
    }

}
