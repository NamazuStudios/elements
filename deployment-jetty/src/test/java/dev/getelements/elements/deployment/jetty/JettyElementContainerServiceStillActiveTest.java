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
 * Regression coverage for #209: when a hot reload fails, the runtime service keeps the deployment's FAILED
 * record in its active runtimes (so it can retry with backoff). The container service must not treat that
 * FAILED record as "still active" when a {@code RUNTIME_UNLOADED} event arrives — the old container's elements
 * were already closed by the reload, so the stale container must be unmounted rather than kept serving.
 */
public class JettyElementContainerServiceStillActiveTest {

    @Test
    public void failedRuntimeIsNotStillActive() {
        final var record = runtimeRecord("deployment-1", ElementRuntimeService.RuntimeStatus.FAILED);
        assertFalse(JettyElementContainerService.stillActive(List.of(record), "deployment-1"),
                "a FAILED runtime must not count as still active: the old container must be unmounted");
    }

    @Test
    public void liveRuntimeIsStillActive() {
        final var record = runtimeRecord("deployment-1", ElementRuntimeService.RuntimeStatus.CLEAN);
        assertTrue(JettyElementContainerService.stillActive(List.of(record), "deployment-1"),
                "a live runtime counts as still active (the deployment was reloaded, not removed)");
    }

    @Test
    public void unknownDeploymentIsNotStillActive() {
        assertFalse(JettyElementContainerService.stillActive(emptyList(), "deployment-1"));
    }

    @Test
    public void otherDeploymentsDoNotMatch() {
        final var record = runtimeRecord("deployment-2", ElementRuntimeService.RuntimeStatus.CLEAN);
        assertFalse(JettyElementContainerService.stillActive(List.of(record), "deployment-1"));
    }

    private static RuntimeRecord runtimeRecord(final String deploymentId,
                                               final ElementRuntimeService.RuntimeStatus status) {
        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(deploymentId);
        final var record = mock(RuntimeRecord.class);
        when(record.deployment()).thenReturn(deployment);
        when(record.status()).thenReturn(status);
        return record;
    }

}
