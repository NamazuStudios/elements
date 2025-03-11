package dev.getelements.elements.rest.status;

import dev.getelements.elements.sdk.model.exception.UnhealthyException;
import dev.getelements.elements.sdk.model.health.HealthStatus;
import dev.getelements.elements.sdk.service.health.HealthStatusService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.sdk.model.health.HealthStatus.HEALTHY_THRESHOLD;

@Path("health")
public class HealthResource {

    private HealthStatusService healthStatusService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Performs the health check.",
            description = "Performs the health check for the server. What this actually does is deployment and " +
                          "implementation specific. However, any successful response code should indicate that the " +
                          "service is capable of servicing requests. Any unsuccessful error codes should indicate " +
                          "that the instance has internal issues and should be taken offline.")
    public HealthStatus getServerHealth() {

        final var healthStatus = getHealthStatusService().checkHealthStatus();

        if (healthStatus.getOverallHealth() < HEALTHY_THRESHOLD) {
            throw new UnhealthyException(healthStatus);
        }

        return healthStatus;

    }

    public HealthStatusService getHealthStatusService() {
        return healthStatusService;
    }

    @Inject
    public void setHealthStatusService(HealthStatusService healthStatusService) {
        this.healthStatusService = healthStatusService;
    }

}
