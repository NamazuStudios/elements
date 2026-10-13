package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.status.HealthResource;
import dev.getelements.elements.sdk.model.exception.UnhealthyException;
import dev.getelements.elements.sdk.model.health.HealthStatus;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.health.HealthStatusService;
import dev.getelements.elements.sdk.service.user.UserService;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.model.user.User.Level.UNPRIVILEGED;
import static jakarta.ws.rs.core.MediaType.valueOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Covers the caller-dependent branches of {@link HealthResource} which require an unhealthy instance, a state the
 * embedded integration instance cannot easily be driven into. The healthy paths and the real startup wiring are covered
 * by {@link HealthResourceIntegrationTest}.
 */
public class HealthResourceTest {

    private static final double HEALTHY = 100.0;

    private static final double UNHEALTHY = 50.0;

    @Test
    public void anonymousCallerReceivesBareOk() {
        final var response = resource(UNPRIVILEGED, HEALTHY).getServerHealth();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertTrueTextPlain(response);
        assertEquals(response.getEntity(), "OK");
    }

    @Test
    public void userCallerReceivesBareOk() {
        final var response = resource(User.Level.USER, HEALTHY).getServerHealth();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertTrueTextPlain(response);
        assertEquals(response.getEntity(), "OK");
    }

    @Test
    public void nonSuperuserReceivesGenericServiceUnavailable() {
        final var response = resource(UNPRIVILEGED, UNHEALTHY).getServerHealth();

        assertEquals(response.getStatus(), Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        assertTrueTextPlain(response);
        assertEquals(response.getEntity(), "Unhealthy");
    }

    @Test
    public void superuserReceivesFullHealthStatus() {
        final var healthStatus = new HealthStatus();

        final var response = resource(SUPERUSER, HEALTHY, healthStatus).getServerHealth();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertTrue(response.getMediaType().isCompatible(valueOf(MediaType.APPLICATION_JSON)),
                "superuser callers should receive JSON, was: " + response.getMediaType());
        assertSame(response.getEntity(), healthStatus);
    }

    @Test
    public void superuserReceivesUnhealthyExceptionWhenUnhealthy() {
        final var healthStatus = new HealthStatus();

        final var thrown = expectThrows(UnhealthyException.class,
                () -> resource(SUPERUSER, UNHEALTHY, healthStatus).getServerHealth());

        assertSame(thrown.getHealthStatus(), healthStatus,
                "superuser callers should receive the health status document explaining the failure");
    }

    @Test
    public void missingUserIsTreatedAsNotSuperuser() {
        final var userService = mock(UserService.class);
        when(userService.getCurrentUser()).thenReturn(null);

        final var status = new HealthStatus();
        status.setOverallHealth(100.0);
        final var healthStatusService = mock(HealthStatusService.class);
        when(healthStatusService.checkHealthStatus()).thenReturn(status);

        final var resource = new HealthResource();
        resource.setHealthStatusService(healthStatusService);
        resource.setUserService(userService);

        final var response = resource.getServerHealth();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getEntity(), "OK");
    }

    private static void assertTrueTextPlain(final Response response) {
        assertTrue(response.getMediaType().isCompatible(valueOf(MediaType.TEXT_PLAIN)),
                "callers without superuser access should receive text/plain, was: " + response.getMediaType());
    }

    private static HealthResource resource(final User.Level level, final double overallHealth) {
        return resource(level, overallHealth, new HealthStatus());
    }

    private static HealthResource resource(final User.Level level,
                                          final double overallHealth,
                                          final HealthStatus healthStatus) {

        healthStatus.setOverallHealth(overallHealth);

        final var healthStatusService = mock(HealthStatusService.class);
        when(healthStatusService.checkHealthStatus()).thenReturn(healthStatus);

        final var user = new User();
        user.setLevel(level);

        final var userService = mock(UserService.class);
        when(userService.getCurrentUser()).thenReturn(user);

        final var resource = new HealthResource();
        resource.setHealthStatusService(healthStatusService);
        resource.setUserService(userService);

        return resource;
    }

}
