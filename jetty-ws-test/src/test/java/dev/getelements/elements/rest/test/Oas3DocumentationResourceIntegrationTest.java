package dev.getelements.elements.rest.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static org.testng.Assert.*;

public class Oas3DocumentationResourceIntegrationTest {

    private static final String OPENAPI_JSON_URL = "/openapi.json";
    private static final String OPENAPI_YAML_URL = "/openapi.yaml";

    @Factory
    public Object[] getTests() {
        return new Object[]{
                TestUtils.getInstance().getTestFixture(Oas3DocumentationResourceIntegrationTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Test
    public void testGetOpenApiJsonReturns200() {
        final var status = client
                .target(apiRoot + OPENAPI_JSON_URL)
                .request(MediaType.APPLICATION_JSON)
                .get()
                .getStatus();

        assertEquals(status, Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetOpenApiYamlReturns200() {
        final var status = client
                .target(apiRoot + OPENAPI_YAML_URL)
                .request("application/yaml")
                .get()
                .getStatus();

        assertEquals(status, Response.Status.OK.getStatusCode());
    }

    @Test
    public void testJsonSpecContainsVersion() throws Exception {
        final var body = client
                .target(apiRoot + OPENAPI_JSON_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        final var spec = new ObjectMapper().readValue(body, Map.class);
        final var info = (Map<?, ?>) spec.get("info");

        assertNotNull(info, "info object should be present");
        assertNotNull(info.get("version"), "info.version should be set");
        assertFalse(((String) info.get("version")).isBlank(), "info.version should not be blank");
    }

    @Test
    public void testJsonSpecContainsServerUrl() throws Exception {
        final var body = client
                .target(apiRoot + OPENAPI_JSON_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        final var spec = new ObjectMapper().readValue(body, Map.class);
        final var servers = (List<?>) spec.get("servers");

        assertNotNull(servers, "servers list should be present");
        assertFalse(servers.isEmpty(), "at least one server URL should be defined");
    }

    @Test
    public void testJsonSpecContainsSecuritySchemes() throws Exception {
        final var body = client
                .target(apiRoot + OPENAPI_JSON_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        final var spec = new ObjectMapper().readValue(body, Map.class);
        final var components = (Map<?, ?>) spec.get("components");

        assertNotNull(components, "components should be present");

        final var securitySchemes = (Map<?, ?>) components.get("securitySchemes");

        assertNotNull(securitySchemes, "securitySchemes should be present");
        assertTrue(securitySchemes.containsKey("auth_bearer"), "auth_bearer scheme should be present");
        assertTrue(securitySchemes.containsKey("session_secret"), "session_secret scheme should be present");
    }

    @Test
    public void testJsonSpecContainsErrorResponseSchema() throws Exception {
        final var body = client
                .target(apiRoot + OPENAPI_JSON_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        final var spec = new ObjectMapper().readValue(body, Map.class);
        final var components = (Map<?, ?>) spec.get("components");

        assertNotNull(components, "components should be present");

        final var schemas = (Map<?, ?>) components.get("schemas");

        assertNotNull(schemas, "schemas should be present");
        assertTrue(schemas.containsKey("ErrorResponse"), "ErrorResponse schema should be injected by EnhancedSpecFilter");
    }

    @Test
    public void testJsonSpecContainsElementsCoreTag() throws Exception {
        final var body = client
                .target(apiRoot + OPENAPI_JSON_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        final var spec = new ObjectMapper().readValue(body, Map.class);
        final var tags = (List<Map<?, ?>>) spec.get("tags");

        assertNotNull(tags, "tags list should be present");

        final var found = tags.stream()
                .anyMatch(tag -> "ElementsCore".equals(tag.get("name")));

        assertTrue(found, "ElementsCore tag should be injected by EnhancedSpecFilter");
    }

}
