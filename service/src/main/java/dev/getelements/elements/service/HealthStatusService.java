package dev.getelements.elements.service;

import dev.getelements.elements.model.health.HealthStatus;

/**
 * Generates a {@link HealthStatus}
 */
public interface HealthStatusService {

    /**
     * Performs some health checks on the server and then returns the status. If any health check fails this will throw
     * an exception. If the system is healthy, this will return the {@link HealthStatus}.
     *
     * @return the {@link HealthStatus}
     */
    HealthStatus checkHealthStatus();

}
