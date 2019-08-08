package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.List;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.EXCEPTION;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.UNKNOWN_ERROR;
import static java.lang.Thread.interrupted;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQRoutingServer implements AutoCloseable {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRoutingServer.class);

    private static final long POLL_TIMEOUT_MILLISECONDS = 5000;

    private final ZContext zContextShadow;

    private final ZMQ.Poller poller;

    private final JeroMQCommandServer control;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    public JeroMQRoutingServer(final InstanceId instanceId,
                               final ZContext zContext,
                               final List<String> bindAddresses) {

        this.zContextShadow = shadow(zContext);
        this.poller = zContextShadow.createPoller(0);

        final ZMQ.Socket main = zContextShadow.createSocket(ROUTER);
        bindAddresses.forEach(main::bind);

        final int frontend = poller.register(main, POLLIN | POLLERR);
        this.multiplex = new JeroMQMultiplexRouter(zContextShadow, poller);
        this.demultiplex = new JeroMQDemultiplexRouter(zContextShadow, poller, frontend);
        this.control = new JeroMQCommandServer(instanceId, poller, frontend, multiplex, demultiplex);

    }

    public void run() {
        while (!interrupted()) {

            if (poller.poll(POLL_TIMEOUT_MILLISECONDS) < 0) {
                logger.info("Poller signaled interruption.  Exiting.");
                break;
            }

            try {
                control.poll();
                multiplex.poll();
                demultiplex.poll();
            } catch (Exception ex) {
                logger.error("Caught exception in routing server.", ex);
            }

        }

    }

    @Override
    public void close() {
        poller.close();
        zContextShadow.close();
    }


    public static ZMsg error(final JeroMQControlResponseCode code, final String message) {
        final ZMsg response = new ZMsg();
        (code == null ? UNKNOWN_ERROR : code).pushResponseCode(response);
        response.addLast(message.getBytes(CHARSET));
        return response;
    }

    public static ZMsg exceptionError(final Exception ex) {
        final ZMsg response = exceptionError(EXCEPTION, ex);
        return response;
    }

    public static ZMsg exceptionError(final JeroMQControlResponseCode code, final Exception ex) {

        logger.error("Exception processing request.", ex);
        final ZMsg response = new ZMsg();

        EXCEPTION.pushResponseCode(response);
        response.addLast(ex.getMessage().getBytes(CHARSET));

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(ex);
            }

            response.addLast(bos.toByteArray());

        } catch (IOException e) {
            logger.error("Caught exception serializing exception.", e);
        }

        return response;
    }

}
