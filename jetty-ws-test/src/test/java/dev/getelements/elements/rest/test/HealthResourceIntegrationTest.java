package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.health.HealthStatus;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static jakarta.ws.rs.core.MediaType.valueOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Verifies the caller-dependent shape of the health endpoint. Infrastructure probes and untrusted callers only learn
 * whether the instance is healthy, while SUPERUSER callers get the full health status document, including the details
 * of any unhealthy mission critical deployments.
 */
public class HealthResourceIntegrationTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                getInstance().getTestFixture(HealthResourceIntegrationTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private ClientContext superuserContext;

    private ClientContext userContext;

    @BeforeClass
    private void createSessions() {
        superuserContext = clientContextProvider.get()
                .createSuperuser("HealthTestSuperuser")
                .createSession();

        userContext = clientContextProvider.get()
                .createUser("HealthTestUser")
                .createSession();
    }

    @Test
    public void testAnonymousCallerReceivesBareOk() {
        final var response = client
                .target(apiRoot + "/health")
                .request()
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertTrue(response.getMediaType().isCompatible(valueOf(MediaType.TEXT_PLAIN)),
                "anonymous callers should receive text/plain, was: " + response.getMediaType());
        assertEquals(response.readEntity(String.class), "OK", "anonymous callers must not receive health details");
    }

    @Test
    public void testUserCallerReceivesBareOk() {
        final var response = client
                .target(apiRoot + "/health")
                .request()
                .header(SESSION_SECRET, userContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertTrue(response.getMediaType().isCompatible(valueOf(MediaType.TEXT_PLAIN)),
                "user callers should receive text/plain, was: " + response.getMediaType());
        assertEquals(response.readEntity(String.class), "OK", "user callers must not receive health details");
    }

    @Test
    public void testSuperuserReceivesFullHealthStatus() {
        final var response = client
                .target(apiRoot + "/health")
                .request()
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertTrue(response.getMediaType().isCompatible(valueOf(MediaType.APPLICATION_JSON)),
                "superuser callers should receive JSON, was: " + response.getMediaType());

        final var healthStatus = response.readEntity(HealthStatus.class);

        assertNotNull(healthStatus, "superuser callers should receive the health status document");
        assertEquals(healthStatus.getOverallHealth(), 100.0,
                "the test instance has no mission critical deployments, so it should be fully healthy");
    }

}
