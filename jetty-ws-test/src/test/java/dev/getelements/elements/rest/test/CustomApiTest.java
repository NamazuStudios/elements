package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import dev.getelements.elements.sdk.model.system.ElementContainerStatus;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import dev.getelements.elements.sdk.model.system.ElementRuntimeStatus;
import dev.getelements.elements.sdk.model.system.ElementSpi;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.TEST_APP_SERVE_RS_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.test.TestElementArtifact.API;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;

public class CustomApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(CustomApiTest.class)
        };
    }

    private static final String MESSAGE_ENDPOINT = "/myapp/message";

    private static final String OPENAPI_ENDPOINT = "/myapp/openapi.json";

    @Inject
    private Client client;

    @Inject
    @Named(TEST_APP_SERVE_RS_ROOT)
    private String appServeRoot;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private ElementRuntimeService runtimeService;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private ClientContext superuserContext;

    private Message message;

    @BeforeClass
    public void deployCustomApi() {

        superuserContext = clientContextProvider.get()
                .createSuperuser("CustomApiTestSuperuser")
                .createSession();

        final var loader = ElementArtifactLoader.newDefaultInstance();

        final var spiClasspath = loader.findClasspathForArtifact(
                ArtifactRepository.DEFAULTS,
                GUICE_7_0_X.getCoordinates()
        ).toList();

        final var resApiClasspath = loader.findClasspathForArtifact(
                ArtifactRepository.DEFAULTS,
                JAKARTA_RS.getCoordinates()
        ).toList();

        if (spiClasspath.isEmpty()) {
            throw new IllegalStateException(
                    ("%s artifact not found. Make sure you ran `mvn -DskipTests install` on the whole project before " +
                      "running this test.").formatted(GUICE_7_0_X.getAllCoordinates())
            );
        }

        if (resApiClasspath.isEmpty()) {
            throw new IllegalStateException(
                    ("%s artifact not found. Make sure you ran `mvn -DskipTests install` on " +
                     "the whole project before running this test.").formatted(resApiClasspath)
            );
        }

        final var restApiDeployment = TransientDeploymentRequest.builder()
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

        runtimeService.loadTransientDeployment(restApiDeployment);

    }

    @Test
    public void createNewMessage() {

        final var toCreate = new CreateMessageRequest();
        toCreate.setMessage("Hello World!");

        final var response = client
                .target(appServeRoot + MESSAGE_ENDPOINT)
                .request()
                .post(Entity.entity(toCreate, APPLICATION_JSON));

        message = response.readEntity(Message.class);
        Assert.assertNotNull(message);
        Assert.assertEquals(message.getMessage(), "Hello World!");
        Assert.assertEquals(message.getCreated(), message.getUpdated());

    }

    @Test(dependsOnMethods = "createNewMessage")
    public void updateExistingMessage() {

        final var toUpdate = new UpdateMessageRequest();
        toUpdate.setMessage("Hello World Again!");

        final var response = client
                .target(appServeRoot + format("%s/%s", MESSAGE_ENDPOINT, message.getId()))
                .request()
                .put(Entity.entity(toUpdate, APPLICATION_JSON));

        message = response.readEntity(Message.class);
        Assert.assertNotNull(message);
        Assert.assertEquals(message.getMessage(), "Hello World Again!");
        Assert.assertNotEquals(message.getCreated(), message.getUpdated());

    }

    @Test(dependsOnMethods = "updateExistingMessage")
    public void testGetMessage() {

        final var response = client
                .target(appServeRoot + format("%s/%s", MESSAGE_ENDPOINT, message.getId()))
                .request()
                .get();

        final var message = response.readEntity(Message.class);
        checkMessage(message);

    }

    @Test(dependsOnMethods = "updateExistingMessage")
    public void testGetMessages() {
        final var response = client
                .target(appServeRoot + MESSAGE_ENDPOINT)
                .request()
                .get();

        final var messages = response.readEntity(GetManyResponse.class);
        checkMessage(messages.getFirst());

    }

    @Test(dependsOnMethods = {"testGetMessage", "testGetMessages"})
    public void testDeleteMessage() {

        final var response = client
                .target(appServeRoot + MESSAGE_ENDPOINT)
                .request()
                .get();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void testRuntimeContainsElement() {

        final var response = client
                .target(apiRoot + "/elements/runtime")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get();

        final var runtimes = response.readEntity(new GenericType<List<ElementRuntimeStatus>>(){});

        final var found = runtimes.stream()
                .flatMap(runtime -> runtime.elements().stream())
                .anyMatch(element -> "dev.getelements.elements.sdk.test.element.rs".equals(element.definition().name()));

        Assert.assertTrue(found, "Expected element 'dev.getelements.elements.sdk.test.element.rs' not found in any runtime.");

    }

    @Test
    public void testContainerContainsElement() {

        final var containers = client
                .target(apiRoot + "/elements/container")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get(new GenericType<List<ElementContainerStatus>>() {});

        final var found = containers.stream()
                .flatMap(container -> container.elements().stream())
                .anyMatch(element -> "dev.getelements.elements.sdk.test.element.rs".equals(element.definition().name()));

        Assert.assertTrue(found, "Expected element 'dev.getelements.elements.sdk.test.element.rs' not found in any container.");

    }

    @Test
    public void testOpenApiSpecIsAvailable() {

        final var response = client
                .target(appServeRoot + OPENAPI_ENDPOINT)
                .request(APPLICATION_JSON)
                .get();

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var body = response.readEntity(String.class);
        Assert.assertNotNull(body);
        Assert.assertFalse(body.isBlank(), "Expected a non-empty OpenAPI specification.");

    }

    @Test
    public void testBuiltinSpisAreReturned() {

        final var spis = client
                .target(apiRoot + "/elements/builtin_spi")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get(new GenericType<List<ElementSpi>>() {});

        Assert.assertNotNull(spis);
        Assert.assertFalse(spis.isEmpty(), "Expected at least one builtin SPI to be returned.");
        spis.forEach(spi -> {
            Assert.assertNotNull(spi.id(), "SPI id must not be null");
            Assert.assertNotNull(spi.version(), "SPI version must not be null");
            Assert.assertNotNull(spi.coordinates(), "SPI coordinates must not be null");
            Assert.assertFalse(spi.coordinates().isEmpty(), "SPI coordinates must not be empty");
        });

    }

    private void checkMessage(final Message message) {
        Assert.assertNotNull(message);
        Assert.assertEquals(message.getMessage(), this.message.getMessage());
        Assert.assertEquals(message.getUpdated(), this.message.getUpdated());
        Assert.assertEquals(message.getCreated(), this.message.getCreated());
    }

    private static class Message {

        private int id;

        private long created;

        private long updated;

        private String message;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(long updated) {
            this.updated = updated;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

    public static class CreateMessageRequest {

        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

    public static class UpdateMessageRequest {

        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

    private static class GetManyResponse extends ArrayList<Message> {}

}
