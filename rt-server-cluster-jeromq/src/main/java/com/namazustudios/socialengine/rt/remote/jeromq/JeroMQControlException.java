package com.namazustudios.socialengine.rt.remote.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.lang.String.format;

public class JeroMQControlException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQControlException.class);

    public JeroMQControlException(final JeroMQControlResponseCode code, final ZMsg response) {
        super(message(code, response), cause(response));
    }

    private static String message(final JeroMQControlResponseCode code, final ZMsg response) {
        final String message = response.isEmpty() ? "unknown" : response.removeFirst().getString(CHARSET);
        return format("%s - %s", code, message);
    }

    private static Throwable cause(final ZMsg response) {

        if (response.isEmpty()) return null;

        final byte[] bytes = response.removeFirst().getData();

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (Throwable) ois.readObject();
        } catch (IOException | ClassCastException | ClassNotFoundException e) {
            logger.error("Caught exception deserializing cause.", e);
            return e;
        }

    }

}
