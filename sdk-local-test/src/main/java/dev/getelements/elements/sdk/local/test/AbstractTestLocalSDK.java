package dev.getelements.elements.sdk.local.test;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractTestLocalSDK {

    /**
     * Returns the application path prefix used to route requests to the deployed Element,
     * e.g. {@code "myapp"} or {@code "myapp_maven"}.
     */
    protected abstract String appPath();

    @Test
    public void testCallVersionEndpoint() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client.target("http://localhost:8181/api/rest/version")
                    .request()
                    .get();

            Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        }
    }

    @Test
    public void testGetMessages() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client.target("http://localhost:8181/app/rest/" + appPath() + "/message")
                    .request()
                    .get();

            Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

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
                URI.create("ws://localhost:8181/app/ws/" + appPath() + "/echo"))) {

            session.getBasicRemote().sendText(testMessage);

            final var received = latch.await(5, TimeUnit.SECONDS);
            Assert.assertTrue(received, "Did not receive echo response within timeout");
            Assert.assertEquals(receivedMessage.get(), testMessage, "Echo message does not match sent message");
        }
    }

}