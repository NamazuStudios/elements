package dev.getelements.elements.rest;

import dev.getelements.elements.exception.UnhealthyException;
import dev.getelements.elements.model.health.HealthStatus;
import dev.getelements.elements.service.HealthStatusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.model.health.HealthStatus.HEALTHY_THRESHOLD;

@Api(hidden = true,
     value = "Service Health",
     description = "Performs basic internal health checks and returns a successful response if the health checks " +
                   "pass. This endpoint should not be exposed in production environments and should remain hidden " +
                   "behind a load balancer or firewall as exposing it could potentially be used to cause denial of " +
                   "service attacks.")
@Path("health")
public class HealthResource {

    private HealthStatusService healthStatusService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Performs the health check.",
                  notes = "Performs the health check for the server. What this actually does is deployment and " +
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
