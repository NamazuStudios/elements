package com.namazustudios.socialengine.rt.remote.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.UNKNOWN_ERROR;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.lang.String.format;

public class JeroMQControlException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQControlException.class);

    private final JeroMQControlResponseCode code;

    public JeroMQControlException(final JeroMQControlException cause) {
        super(cause);
        this.code = cause.getCode();
    }

    public JeroMQControlException(final JeroMQControlResponseCode code) {
        super(code == null ? UNKNOWN_ERROR.toString() : code.toString());
        this.code = code;
    }

    public JeroMQControlException(final JeroMQControlResponseCode code, final ZMsg response) {
        super(message(code, response), cause(response));
        this.code = code;
    }

    private static String message(final JeroMQControlResponseCode code, final ZMsg response) {
        final var message = response.isEmpty() ? "unknown" : response.removeFirst().getString(CHARSET);
        return format("%s - %s", code, message);
    }

    public JeroMQControlResponseCode getCode() {
        return code;
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
