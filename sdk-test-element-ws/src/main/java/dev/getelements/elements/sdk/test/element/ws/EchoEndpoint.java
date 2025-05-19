package dev.getelements.elements.sdk.test.element.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/echo")
public class EchoEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(EchoEndpoint.class);

    @OnOpen
    public void onOpen(final Session session) {
        final var request = session.getRequestParameterMap();
        logger.info("Opened {}", session.getId());
    }

    @OnMessage
    public String onMessage(final Session session, final String message) {
        logger.info("Received {}. Echoing.", message);
        return message;
    }

    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        logger.info("Closed {} - {}", session.getId(), closeReason);
    }

}
