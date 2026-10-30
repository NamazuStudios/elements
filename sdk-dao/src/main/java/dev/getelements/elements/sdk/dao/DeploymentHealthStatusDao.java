package dev.getelements.elements.sdk.dao;

import java.util.List;

/**
 * Provides the deployment related portion of the system's health status. Deployments are only observable where an
 * Element runtime service exists, so implementations are contributed by the modules which host one. When no
 * implementation is present, the deployment portion of the health check is skipped.
 *
 * <p>This is a service extension point rather than a storage abstraction, so implementations are contributed to the
 * multibound set by each module which hosts an Element runtime service rather than exposed to Elements as a DAO.
 */
public interface DeploymentHealthStatusDao {

    /**
     * Returns a description of each Element deployment which is flagged as mission critical but is not currently
     * healthy. A mission critical deployment is unhealthy when the runtime fails to load it entirely, or when loading
     * completes with errors such that at least one Element in the deployment did not load. Deployments which are not
     * flagged as mission critical never affect the result.
     *
     * @return the unhealthy mission critical deployments, or an empty list if all are healthy
     */
    List<String> getUnhealthyMissionCriticalDeployments();

}
