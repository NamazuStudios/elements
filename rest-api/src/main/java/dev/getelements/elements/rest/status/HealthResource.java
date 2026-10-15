package dev.getelements.elements.rest.status;

import dev.getelements.elements.sdk.model.exception.UnhealthyException;
import dev.getelements.elements.sdk.model.health.HealthStatus;
import dev.getelements.elements.sdk.service.health.HealthStatusService;
import dev.getelements.elements.sdk.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static dev.getelements.elements.sdk.model.health.HealthStatus.HEALTHY_THRESHOLD;
import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;

@Path("health")
public class HealthResource {

    private static final String HEALTHY_RESPONSE = "OK";

    private static final String UNHEALTHY_RESPONSE = "Unhealthy";

    private HealthStatusService healthStatusService;

    private UserService userService;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Performs the health check.",
            description = "Performs the health check for the server. What this actually does is deployment and " +
                          "implementation specific. However, any successful response code should indicate that the " +
                          "service is capable of servicing requests. Any unsuccessful error codes should indicate " +
                          "that the instance has internal issues and should be taken offline. Callers with SUPERUSER " +
                          "access receive the full health status document. All other callers receive a bare indication " +
                          "of health, without any details, so that infrastructure probes and untrusted callers " +
                          "cannot inspect the internals of the instance.")
    public Response getServerHealth() {

        final var healthStatus = getHealthStatusService().checkHealthStatus();
        final var healthy = healthStatus.getOverallHealth() >= HEALTHY_THRESHOLD;

        if (isSuperuser()) {
            if (!healthy) {
                throw new UnhealthyException(healthStatus);
            }
            return Response.ok(healthStatus).type(MediaType.APPLICATION_JSON).build();
        }

        if (!healthy) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(UNHEALTHY_RESPONSE)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        return Response.ok(HEALTHY_RESPONSE).type(MediaType.TEXT_PLAIN).build();

    }

    private boolean isSuperuser() {
        final var user = getUserService().getCurrentUser();
        return user != null && SUPERUSER.equals(user.getLevel());
    }

    public HealthStatusService getHealthStatusService() {
        return healthStatusService;
    }

    @Inject
    public void setHealthStatusService(HealthStatusService healthStatusService) {
        this.healthStatusService = healthStatusService;
    }

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
