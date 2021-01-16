package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.jeromq.JeroMQMonitorThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.EXCEPTION;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.UNKNOWN_ERROR;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static zmq.ZError.EAGAIN;

public class JeroMQRoutingServer implements AutoCloseable {

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Logger logger;

    private static final long POLL_TIMEOUT_MILLISECONDS = 1000;

    private final ZContext zContextShadow;

    private final ZMQ.Poller poller;

    private final JeroMQCommandServer control;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    private final JeroMQMonitorThread monitorThread;

    public JeroMQRoutingServer(final InstanceId instanceId,
                               final ZContext zContext,
                               final List<String> bindAddresses) {

        this.logger = getLogger(instanceId);
        this.zContextShadow = shadow(zContext);
        this.poller = zContextShadow.createPoller(0);

        final var main = zContextShadow.createSocket(ROUTER);
        bindAddresses.forEach(main::bind);

        final var frontend = poller.register(main, POLLIN | POLLERR);
        this.multiplex = new JeroMQMultiplexRouter(instanceId, zContextShadow, poller);
        this.demultiplex = new JeroMQDemultiplexRouter(instanceId, zContextShadow, poller, frontend);
        this.control = new JeroMQCommandServer(instanceId, poller, frontend, multiplex, demultiplex);
        this.monitorThread = new JeroMQMonitorThread(JeroMQRoutingServer.class.getSimpleName(), logger, zContext, main);
        this.monitorThread.start();

    }

    public void run(final BooleanSupplier running) {
        while (running.getAsBoolean()) {

            if (poller.poll(POLL_TIMEOUT_MILLISECONDS) < 0 || interrupted()) {
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

            final var size = poller.getSize();

            for (var index = 0; index < size; ++index) {
                final var item = poller.getItem(index);
                if (item == null) continue;
                final var err = item.getSocket().errno();
                if (err != 0 && err != EAGAIN) logger.error("Socket got errno: {}", err);
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

    public static ZMsg exceptionError(final Logger logger, final Exception ex) {
        final ZMsg response = exceptionError(logger, EXCEPTION, ex);
        return response;
    }

    public static ZMsg exceptionError(final Logger logger, final JeroMQControlResponseCode code, final Exception ex) {

        logger.error("Exception processing request.", ex);
        final ZMsg response = new ZMsg();

        code.pushResponseCode(response);
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

    private static Logger getLogger(final InstanceId instanceId) {
        return getLogger(JeroMQRoutingServer.class, instanceId);
    }

    public static Logger getLogger(final Class<?> componentClass, final InstanceId instanceId) {
        return LoggerFactory.getLogger(format("%s.%s", componentClass.getSimpleName(), instanceId.asString()));
    }

}
