package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import dev.getelements.elements.sdk.model.codegen.CodegenRequest;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import dev.getelements.elements.sdk.record.ArtifactRepository;
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
import java.util.Map;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.TEST_APP_SERVE_RS_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.test.TestElementArtifact.API;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static org.testng.Assert.*;

public class CodegenResourceIntegrationTest {

    // The test element exposes its OpenAPI spec at this path under appServeRoot
    private static final String ELEMENT_OPENAPI_YAML = "/myapp/openapi.yaml";

    private static final String CODEGEN_URL = "/codegen";

    // "html2" is a lightweight OpenAPI generator that produces a single file quickly
    private static final String LANGUAGE = "html2";

    private static final String PACKAGE_NAME = "dev.getelements.test";

    @Factory
    public Object[] getTests() {
        return new Object[]{
                TestUtils.getInstance().getTestFixture(CodegenResourceIntegrationTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    @Named(TEST_APP_SERVE_RS_ROOT)
    private String appServeRoot;

    @Inject
    private Client client;

    @Inject
    private ElementRuntimeService runtimeService;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private ClientContext superuserContext;

    private ClientContext userContext;

    @BeforeClass
    public void setUp() {
        superuserContext = clientContextProvider.get()
                .createSuperuser("CodegenTestSuperuser")
                .createSession();

        userContext = clientContextProvider.get()
                .createUser("CodegenTestUser")
                .createSession();

        final var loader = ElementArtifactLoader.newDefaultInstance();

        final var spiClasspath = loader.findClasspathForArtifact(
                ArtifactRepository.DEFAULTS,
                GUICE_7_0_X.getCoordinates()
        ).toList();

        if (spiClasspath.isEmpty()) {
            throw new IllegalStateException(
                    ("%s artifact not found. Run `mvn -DskipTests install` before running this test.")
                            .formatted(GUICE_7_0_X.getAllCoordinates())
            );
        }

        final var deployment = TransientDeploymentRequest.builder()
                .useDefaultRepositories(true)
                .addElement(new ElementPathDefinition(
                        "rs",
                        API.getAllCoordinates().toList(),
                        List.of("DEFAULT"),
                        List.of(),
                        JAKARTA_RS.getAllCoordinates().toList(),
                        Map.of()
                ))
                .build();

        runtimeService.loadTransientDeployment(deployment);
    }

    // -- Core codegen tests --

    @Test
    public void testGenerateCoreCodeReturnsZip() {
        final var request = new CodegenRequest();
        request.language = LANGUAGE;
        request.packageName = PACKAGE_NAME;

        final var response = client
                .target(apiRoot + CODEGEN_URL)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .post(Entity.json(request));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getMediaType().toString(), MediaType.APPLICATION_OCTET_STREAM);

        final var contentDisposition = response.getHeaderString("Content-Disposition");
        assertNotNull(contentDisposition, "Content-Disposition header should be set");
        assertTrue(contentDisposition.startsWith("attachment"), "response should be a downloadable attachment");

        final var bytes = response.readEntity(byte[].class);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0, "generated ZIP should not be empty");
    }

    // -- Element codegen tests --

    @Test
    public void testGenerateElementCodeReturnsZip() {
        final var request = new CodegenRequest();
        request.elementSpecUrl = appServeRoot + ELEMENT_OPENAPI_YAML;
        request.language = LANGUAGE;
        request.packageName = PACKAGE_NAME;

        final var response = client
                .target(apiRoot + CODEGEN_URL)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .post(Entity.json(request));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getMediaType().toString(), MediaType.APPLICATION_OCTET_STREAM);

        final var bytes = response.readEntity(byte[].class);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0, "generated ZIP for element should not be empty");
    }

    @Test
    public void testGenerateElementCodeFromJsonUrl() {
        final var request = new CodegenRequest();
        request.elementSpecUrl = appServeRoot + "/myapp/openapi.json"; // .json → .yaml conversion exercised here
        request.language = LANGUAGE;
        request.packageName = PACKAGE_NAME;

        final var response = client
                .target(apiRoot + CODEGEN_URL)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .post(Entity.json(request));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var bytes = response.readEntity(byte[].class);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0, "generated ZIP for mixed-case path with .json extension should not be empty");
    }

    // -- Access control tests --

    @Test
    public void testRegularUserCannotGenerateCode() {
        final var request = new CodegenRequest();
        request.language = LANGUAGE;
        request.packageName = PACKAGE_NAME;

        final var status = client
                .target(apiRoot + CODEGEN_URL)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .header(SESSION_SECRET, userContext.getSessionSecret())
                .post(Entity.json(request))
                .getStatus();

        assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void testUnauthenticatedCannotGenerateCode() {
        final var request = new CodegenRequest();
        request.language = LANGUAGE;
        request.packageName = PACKAGE_NAME;

        final var status = client
                .target(apiRoot + CODEGEN_URL)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .post(Entity.json(request))
                .getStatus();

        assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());
    }

}
