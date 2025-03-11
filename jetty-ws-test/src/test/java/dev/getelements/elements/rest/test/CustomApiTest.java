package dev.getelements.elements.rest.test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static dev.getelements.elements.rest.test.TestUtils.TEST_APP_SERVE_RS_ROOT;
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

    @Inject
    private Client client;

    @Inject
    @Named(TEST_APP_SERVE_RS_ROOT)
    private String appServeRoot;

    private Message message;

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
