package dev.getelements.elements.sdk.local.test;

import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.local.ElementsLocalBuilder;
import dev.getelements.elements.sdk.mongo.test.DockerMongoTestInstance;
import dev.getelements.elements.sdk.mongo.test.MongoTestInstance;
import dev.getelements.elements.sdk.util.ShutdownHooks;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static dev.getelements.elements.sdk.model.Constants.HTTP_PORT;
import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_WS;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static java.lang.String.format;

public class TestLocalSDK {

    private ElementsLocal elementsLocal;

    private MongoTestInstance mongoTestInstance;

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(TestLocalSDK.class);

    private static final int TEST_MONGO_PORT = 47000;

    @BeforeClass
    public void setupMongoDb() throws InterruptedException {
        mongoTestInstance = new DockerMongoTestInstance(47000);
        mongoTestInstance.start();
        shutdownHooks.add(mongoTestInstance::stop);
        Thread.sleep(1000);
    }

    @BeforeClass(dependsOnMethods = "setupMongoDb")
    public void setUpLocalRunner() {

        final var configurationSupplier = new DefaultConfigurationSupplier() {
            @Override
            public Properties get() {
                final var properties = super.get();
                properties.put(HTTP_PORT, "8181");
                properties.put(MONGO_CLIENT_URI, format("mongodb://127.0.0.1:%d", TEST_MONGO_PORT));
                return properties;
            }
        };

        elementsLocal = ElementsLocalBuilder.getDefault()
                .withProperties(configurationSupplier.get())
                .withDeployment(builder -> builder
                        .useDefaultRepositories(true)
                        .elementPath()
                            .path("rs")
                            .addSpiArtifacts(GUICE_7_0_X.getAllCoordinates().toList())
                            .addElementArtifacts(JAKARTA_RS.getAllCoordinates().toList())
                            .endElementPath()
                        .elementPath()
                            .path("ws")
                            .addSpiArtifacts(GUICE_7_0_X.getAllCoordinates().toList())
                            .addElementArtifacts(JAKARTA_WS.getAllCoordinates().toList())
                            .endElementPath()
                        .build()
                )
                .build();

        shutdownHooks.add(elementsLocal::close);

    }

    @BeforeClass(dependsOnMethods = {"setUpLocalRunner", "setupMongoDb"})
    public void startInstance() {
        elementsLocal.start();
    }

    @AfterClass
    public void tearDownInstance() {
        elementsLocal.close();
    }

    @AfterClass
    public void tearDownMongoDb() {
        mongoTestInstance.close();
    }

    @Test
    public void testCallVersionEndpoint() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client.target("http://localhost:8181/api/rest/version")
                    .request()
                    .get();

            final var status = response.getStatus();
            Assert.assertEquals(status, Response.Status.OK.getStatusCode());

        }
    }

    @Test
    public void testGetMessages() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client.target("http://localhost:8181/app/rest/myapp/message")
                    .request()
                    .get();

            final var status = response.getStatus();
            Assert.assertEquals(status, Response.Status.OK.getStatusCode());

        }
    }

    @Test
    public void testWebSocketEcho() throws Exception {
        final var container = ContainerProvider.getWebSocketContainer();
        final var testMessage = "Hello WebSocket!";
        final var latch = new CountDownLatch(1);
        final var receivedMessage = new AtomicReference<String>();

        try (final Session session = container.connectToServer(
                new EchoClientEndpoint(receivedMessage, latch),
                URI.create("ws://localhost:8181/app/ws/myapp/echo"))) {

            session.getBasicRemote().sendText(testMessage);

            final var received = latch.await(5, TimeUnit.SECONDS);
            Assert.assertTrue(received, "Did not receive echo response within timeout");
            Assert.assertEquals(receivedMessage.get(), testMessage, "Echo message does not match sent message");
        }

    }

    @ClientEndpoint
    public static class EchoClientEndpoint {

        private final CountDownLatch latch;

        private final AtomicReference<String> receivedMessage;

        public EchoClientEndpoint(AtomicReference<String> receivedMessage, CountDownLatch latch) {
            this.receivedMessage = receivedMessage;
            this.latch = latch;
        }

        @OnMessage
        public void onMessage(final String message) {
            receivedMessage.set(message);
            latch.countDown();
        }

    }

}
