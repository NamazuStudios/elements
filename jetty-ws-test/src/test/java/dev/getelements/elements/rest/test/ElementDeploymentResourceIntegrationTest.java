package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.system.*;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static org.testng.Assert.*;

public class ElementDeploymentResourceIntegrationTest {

    @Factory
    public Object[] getTests() {
        return new Object[]{
                TestUtils.getInstance().getTestFixture(ElementDeploymentResourceIntegrationTest.class)
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

    private String createdDeploymentId;

    @BeforeClass
    private void setUp() {
        // Create a superuser for admin operations
        superuserContext = clientContextProvider.get()
                .createSuperuser("ElementDeploymentTestSuperuser")
                .createSession();

        // Create a regular user to test access control
        userContext = clientContextProvider.get()
                .createUser("ElementDeploymentTestUser")
                .createSession();
    }

    @Test(groups = "createDeployment")
    public void testCreateElementDeploymentWithArtifacts() {
        final var elementDefinition = new ElementPathDefinition(
                "example",
                List.of("com.example:api:1.0.0"),
                List.of("com.example:spi:1.0.0"),
                null,
                null
        );

        final var request = new CreateElementDeploymentRequest(
                null, // global deployment
                List.of(elementDefinition),
                null, // packages
                true,
                List.of(new ElementArtifactRepository("central", "https://repo.maven.apache.org/maven2")),
                null,
                ElementDeploymentState.DISABLED
        );

        final var response = client
                .target(apiRoot + "/elements/deployment")
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .post(Entity.json(request));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var created = response.readEntity(ElementDeployment.class);
        assertNotNull(created);
        assertNotNull(created.id());
        assertNull(created.application());
        assertNotNull(created.elements());
        assertEquals(created.elements().size(), 1);
        assertEquals(created.elements().get(0).apiArtifacts(), elementDefinition.apiArtifacts());
        assertEquals(created.elements().get(0).spiArtifacts(), elementDefinition.spiArtifacts());
        assertTrue(created.useDefaultRepositories());
        assertEquals(created.state(), ElementDeploymentState.DISABLED);
        assertEquals(created.version(), 0L);

        createdDeploymentId = created.id();
    }

    @Test(groups = "createDeployment")
    public void testCreateElementDeploymentWithElementArtifacts() {
        final var elementDefinition = new ElementPathDefinition(
                "example",
                List.of("com.example:api:2.0.0"),
                List.of("com.example:spi:2.0.0"),
                List.of("com.example:element-impl:2.0.0"),
                null
        );

        final var request = new CreateElementDeploymentRequest(
                null,
                List.of(elementDefinition),
                null, // packages
                false,
                List.of(),
                null,
                ElementDeploymentState.UNLOADED
        );

        final var response = client
                .target(apiRoot + "/elements/deployment")
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .post(Entity.json(request));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var created = response.readEntity(ElementDeployment.class);
        assertNotNull(created);
        assertNotNull(created.id());
        assertNotNull(created.elements());
        assertEquals(created.elements().size(), 1);
        assertEquals(created.elements().get(0).elementArtifacts(), elementDefinition.elementArtifacts());
        assertFalse(created.useDefaultRepositories());
        assertEquals(created.state(), ElementDeploymentState.ENABLED);
    }

    @Test(
            groups = "fetchDeployment",
            dependsOnGroups = "createDeployment"
    )
    public void testGetElementDeploymentById() {
        assertNotNull(createdDeploymentId, "Deployment ID should be set from create test");

        final var response = client
                .target(apiRoot + "/elements/deployment/" + createdDeploymentId)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var deployment = response.readEntity(ElementDeployment.class);
        assertNotNull(deployment);
        assertEquals(deployment.id(), createdDeploymentId);
        assertNotNull(deployment.elements());
        assertEquals(deployment.elements().size(), 1);
    }

    @Test(
            groups = "fetchDeployment",
            dependsOnGroups = "createDeployment"
    )
    public void testGetElementDeploymentNotFound() {
        final var response = client
                .target(apiRoot + "/elements/deployment/nonexistent-id")
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test(
            groups = "fetchDeployment",
            dependsOnGroups = "createDeployment"
    )
    public void testGetAllElementDeployments() {
        final Pagination<ElementDeployment> page = client
                .target(apiRoot + "/elements/deployment")
                .queryParam("offset", 0)
                .queryParam("count", 20)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get(ElementDeploymentPagination.class);

        assertNotNull(page);
        assertTrue(page.getTotal() >= 2, "Should have at least 2 deployments created");
        assertNotNull(page.getObjects());
        assertFalse(page.getObjects().isEmpty());
    }

    @Test(
            groups = "fetchDeployment",
            dependsOnGroups = "createDeployment"
    )
    public void testGetElementDeploymentsWithPagination() {
        // First page
        final Pagination<ElementDeployment> firstPage = client
                .target(apiRoot + "/elements/deployment")
                .queryParam("offset", 0)
                .queryParam("count", 1)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get(ElementDeploymentPagination.class);

        assertNotNull(firstPage);
        assertEquals(firstPage.getObjects().size(), 1);

        // Second page
        final Pagination<ElementDeployment> secondPage = client
                .target(apiRoot + "/elements/deployment")
                .queryParam("offset", 1)
                .queryParam("count", 1)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get(ElementDeploymentPagination.class);

        assertNotNull(secondPage);
        assertEquals(secondPage.getObjects().size(), 1);

        // Verify different deployments
        assertNotEquals(
                firstPage.getObjects().get(0).id(),
                secondPage.getObjects().get(0).id()
        );
    }

    @Test(
            groups = "updateDeployment",
            dependsOnGroups = "fetchDeployment"
    )
    public void testUpdateElementDeployment() {
        assertNotNull(createdDeploymentId, "Deployment ID should be set from create test");

        final var elementDefinition = new ElementPathDefinition(
                "example",
                List.of("com.example:api-updated:1.1.0"),
                List.of("com.example:spi-updated:1.1.0"),
                null,
                null
        );

        final var request = new UpdateElementDeploymentRequest(
                List.of(elementDefinition),
                null, // packages
                true,
                List.of(new ElementArtifactRepository("central", "https://repo.maven.apache.org/maven2")),
                null,
                ElementDeploymentState.ENABLED
        );

        final var response = client
                .target(apiRoot + "/elements/deployment/" + createdDeploymentId)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .put(Entity.json(request));


        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var updated = response.readEntity(ElementDeployment.class);
        assertNotNull(updated);
        assertEquals(updated.id(), createdDeploymentId);
        assertNotNull(updated.elements());
        assertEquals(updated.elements().size(), 1);
        assertEquals(updated.elements().get(0).apiArtifacts(), elementDefinition.apiArtifacts());
        assertEquals(updated.elements().get(0).spiArtifacts(), elementDefinition.spiArtifacts());
        assertEquals(updated.state(), ElementDeploymentState.ENABLED);
        assertEquals(updated.version(), 1L, "Version should be incremented after update");
    }

    @Test(
            groups = "updateDeployment",
            dependsOnGroups = "fetchDeployment"
    )
    public void testUpdateElementDeploymentNotFound() {
        final var elementDefinition = new ElementPathDefinition(
                "example",
                List.of(),
                List.of(),
                List.of("com.example:element:1.0.0"),
                null
        );

        final var request = new UpdateElementDeploymentRequest(
                List.of(elementDefinition),
                null, // packages
                false,
                List.of(),
                null,
                ElementDeploymentState.DISABLED
        );

        final var response = client
                .target(apiRoot + "/elements/deployment/nonexistent-id")
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .put(Entity.json(request));

        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test(
            groups = "deleteDeployment",
            dependsOnGroups = "updateDeployment"
    )
    public void testDeleteElementDeployment() {
        assertNotNull(createdDeploymentId, "Deployment ID should be set from create test");

        final var response = client
                .target(apiRoot + "/elements/deployment/" + createdDeploymentId)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test(
            groups = "deleteDeployment",
            dependsOnMethods = "testDeleteElementDeployment"
    )
    public void testGetAfterDelete() {
        assertNotNull(createdDeploymentId, "Deployment ID should be set from create test");

        final var response = client
                .target(apiRoot + "/elements/deployment/" + createdDeploymentId)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test(
            groups = "deleteDeployment",
            dependsOnMethods = "testDeleteElementDeployment"
    )
    public void testDoubleDelete() {
        assertNotNull(createdDeploymentId, "Deployment ID should be set from create test");

        final var response = client
                .target(apiRoot + "/elements/deployment/" + createdDeploymentId)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test(groups = "accessControl")
    public void testRegularUserCannotCreateDeployment() {
        final var elementDefinition = new ElementPathDefinition(
                "example",
                List.of("com.example:api:1.0.0"),
                List.of("com.example:spi:1.0.0"),
                null,
                null
        );

        final var request = new CreateElementDeploymentRequest(
                null,
                List.of(elementDefinition),
                null, // packages
                true,
                List.of(),
                null,
                ElementDeploymentState.DISABLED
        );

        final var response = client
                .target(apiRoot + "/elements/deployment")
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, userContext.getSessionSecret())
                .post(Entity.json(request));

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test(groups = "accessControl")
    public void testRegularUserCannotListDeployments() {
        final var response = client
                .target(apiRoot + "/elements/deployment")
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, userContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test(groups = "accessControl")
    public void testUnauthenticatedRequestFails() {
        final var response = client
                .target(apiRoot + "/elements/deployment")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(response.getStatus(), Response.Status.FORBIDDEN.getStatusCode());
    }

}
