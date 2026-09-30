package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.dao.DeploymentHealthStatusDao;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import jakarta.inject.Inject;

import java.util.List;

import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.FAILED;
import static dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeStatus.UNSTABLE;

/**
 * Reports on Element deployments which are flagged as mission critical. Deployments are considered unhealthy when the
 * runtime failed to load them or when loading completed with errors.
 */
public class StandardDeploymentHealthStatusDao implements DeploymentHealthStatusDao {

    private ElementRuntimeService elementRuntimeService;

    @Override
    public List<String> getUnhealthyMissionCriticalDeployments() {
        return getElementRuntimeService()
                .getActiveRuntimes()
                .stream()
                .filter(runtime -> runtime.deployment().missionCritical())
                .filter(runtime -> isUnhealthy(runtime.status()))
                .map(runtime -> describe(runtime.deployment()))
                .sorted()
                .toList();
    }

    private static boolean isUnhealthy(final RuntimeStatus status) {
        return FAILED.equals(status) || UNSTABLE.equals(status);
    }

    private static String describe(final ElementDeployment deployment) {
        return deployment.name() == null
                ? deployment.id()
                : "%s (%s)".formatted(deployment.name(), deployment.id());
    }

    public ElementRuntimeService getElementRuntimeService() {
        return elementRuntimeService;
    }

    @Inject
    public void setElementRuntimeService(final ElementRuntimeService elementRuntimeService) {
        this.elementRuntimeService = elementRuntimeService;
    }

}
