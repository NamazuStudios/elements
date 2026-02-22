package dev.getelements.elements.sdk.local.test;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;

public abstract class AbstractTestLocalSDK {

    /**
     * Returns the application path prefix used to route requests to the deployed Element,
     * e.g. {@code "myapp"} or {@code "myapp_maven"}.
     */
    protected abstract String appPath();

    private Message message;

    private String messageUrl() {
        return "http://localhost:8181/app/rest/" + appPath() + "/message";
    }

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
    public void testGetOas() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client.target("http://localhost:8181/app/rest/" + appPath() + "/openapi.json")
                    .request()
                    .get();

            Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        }
    }

    @Test
    public void createNewMessage() {
        try (final var client = ClientBuilder.newClient()) {

            final var toCreate = new CreateMessageRequest();
            toCreate.setMessage("Hello World!");

            final var response = client
                    .target(messageUrl())
                    .request()
                    .post(Entity.entity(toCreate, APPLICATION_JSON));

            message = response.readEntity(Message.class);
            Assert.assertNotNull(message);
            Assert.assertEquals(message.getMessage(), "Hello World!");
            Assert.assertEquals(message.getCreated(), message.getUpdated());

        }
    }

    @Test(dependsOnMethods = "createNewMessage")
    public void updateExistingMessage() {
        try (final var client = ClientBuilder.newClient()) {

            final var toUpdate = new UpdateMessageRequest();
            toUpdate.setMessage("Hello World Again!");

            final var response = client
                    .target(format("%s/%s", messageUrl(), message.getId()))
                    .request()
                    .put(Entity.entity(toUpdate, APPLICATION_JSON));

            message = response.readEntity(Message.class);
            Assert.assertNotNull(message);
            Assert.assertEquals(message.getMessage(), "Hello World Again!");
            Assert.assertNotEquals(message.getCreated(), message.getUpdated());

        }
    }

    @Test(dependsOnMethods = "updateExistingMessage")
    public void testGetMessage() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client
                    .target(format("%s/%s", messageUrl(), message.getId()))
                    .request()
                    .get();

            final var fetched = response.readEntity(Message.class);
            checkMessage(fetched);

        }
    }

    @Test(dependsOnMethods = "updateExistingMessage")
    public void testGetMessages() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client
                    .target(messageUrl())
                    .request()
                    .get();

            final var messages = response.readEntity(new GenericType<List<Message>>(){});
            checkMessage(messages.get(0));

        }
    }

    @Test(dependsOnMethods = {"testGetMessage", "testGetMessages"})
    public void testDeleteMessage() {
        try (final var client = ClientBuilder.newClient()) {

            final var response = client
                    .target(messageUrl())
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

}
