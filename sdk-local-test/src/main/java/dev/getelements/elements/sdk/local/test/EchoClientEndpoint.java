package dev.getelements.elements.sdk.local.test;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@ClientEndpoint
public class EchoClientEndpoint {

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
