package dev.getelements.elements.sdk.service.health;

import dev.getelements.elements.sdk.model.health.HealthStatus;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Generates a {@link HealthStatus}
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface HealthStatusService {

    /**
     * Performs some health checks on the server and then returns the status. If any health check fails this will throw
     * an exception. If the system is healthy, this will return the {@link HealthStatus}.
     *
     * @return the {@link HealthStatus}
     */
    HealthStatus checkHealthStatus();

}
