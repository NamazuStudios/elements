package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import dev.getelements.elements.sdk.model.system.ElementContainerStatus;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import dev.getelements.elements.sdk.model.system.ElementRuntimeStatus;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.GenericType;
import jakarta.websocket.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.TEST_APP_SERVE_WS_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_WS;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static java.lang.String.format;

public class CustomWebsocketTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(CustomWebsocketTest.class)
        };
    }

    @Inject
    @Named(TEST_APP_SERVE_WS_ROOT)
    private String appServeRoot;

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ElementRuntimeService runtimeService;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    private ClientContext superuserContext;

    private WebSocketContainer container;

    @BeforeClass
    public void setup() {
        container = ContainerProvider.getWebSocketContainer();
        superuserContext = clientContextProvider.get()
                .createSuperuser("CustomWebsocketTestSuperuser")
                .createSession();
    }

    @BeforeClass
    public void deployCustomWebsockets() {

        final var loader = ElementArtifactLoader.newDefaultInstance();

        final var spiClasspath = loader.findClasspathForArtifact(
                ArtifactRepository.DEFAULTS,
                GUICE_7_0_X.getCoordinates()
        ).toList();

        final var resApiClasspath = loader.findClasspathForArtifact(
                ArtifactRepository.DEFAULTS,
                JAKARTA_WS.getCoordinates()
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
                        List.of(),
                        List.of("DEFAULT"),
                        List.of(),
                        JAKARTA_WS.getAllCoordinates().toList(),
                        Map.of()
                ))
                .build();

        runtimeService.loadTransientDeployment(restApiDeployment);

    }

    @Test
    public void testRuntimeContainsElement() {

        final var runtimes = client
                .target(apiRoot + "/elements/runtime")
                .request()
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get(new GenericType<List<ElementRuntimeStatus>>() {});

        final var found = runtimes.stream()
                .flatMap(runtime -> runtime.elements().stream())
                .anyMatch(element -> "dev.getelements.elements.sdk.test.element.ws".equals(element.definition().name()));

        Assert.assertTrue(found, "Expected element 'dev.getelements.elements.sdk.test.element.ws' not found in any runtime.");

    }

    @Test
    public void testContainerContainsElement() {

        final var containers = client
                .target(apiRoot + "/elements/container")
                .request()
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get(new GenericType<List<ElementContainerStatus>>() {});

        final var found = containers.stream()
                .flatMap(container -> container.elements().stream())
                .anyMatch(element -> "dev.getelements.elements.sdk.test.element.ws".equals(element.definition().name()));

        Assert.assertTrue(found, "Expected element 'dev.getelements.elements.sdk.test.element.ws' not found in any container.");

    }

    @Test
    public void testEcho() throws DeploymentException, IOException, InterruptedException {

        final var uri = URI.create(format("%s/myapp/echo", appServeRoot));

        final var client = new TestClient();

        try (var session = container.connectToServer(client, uri)) {

            client.connect();
            client.send("Hello World!");

            final var response = client.receive();
            Assert.assertEquals(response, "Hello World!");

        }

    }

    @ClientEndpoint
    public static class TestClient {

        private Session session;

        private final CountDownLatch connect = new CountDownLatch(1);

        private final BlockingQueue<Supplier<String>> queue = new ArrayBlockingQueue<>(100);

        @OnOpen
        public void onOpen(Session session) {
            this.session = session;
            connect.countDown();
        }

        @OnMessage
        public void onMessage(String message) {
            queue.add(() -> message);
        }

        @OnError
        public void onError(Throwable t) {
            queue.add(() -> {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new RuntimeException(t);
                }
            });
        }

        @OnClose
        public void onClose() {
            session = null;
        }

        public void connect() throws InterruptedException {
            connect.await();
        }

        public String receive() throws InterruptedException {
            return queue.take().get();
        }

        public void send(String message) throws InterruptedException {
            if (session == null) {
                throw new IllegalStateException("Session not open");
            } else {
                session.getAsyncRemote().sendText(message);
            }
        }

    }

}
