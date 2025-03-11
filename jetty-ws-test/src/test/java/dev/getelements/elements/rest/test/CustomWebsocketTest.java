package dev.getelements.elements.rest.test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.websocket.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static dev.getelements.elements.rest.test.TestUtils.TEST_APP_SERVE_WS_ROOT;
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

    private WebSocketContainer container;

    @BeforeClass
    public void setup() {
        container = ContainerProvider.getWebSocketContainer();
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
