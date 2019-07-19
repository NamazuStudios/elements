package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.charset.Charset;
import java.util.List;

import static java.lang.Thread.interrupted;
import static org.zeromq.SocketType.*;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQRoutingServer implements AutoCloseable {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRoutingServer.class);

    private static final long POLL_TIMEOUT_MILLISECONDS = 5000;

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final JeroMQControlServer control;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    public JeroMQRoutingServer(final InstanceId instanceId, final ZContext zContext,
                               final List<String> controlAddresses, final List<String> invokerAddresses) {

        this.zContext = shadow(zContext);
        this.poller = zContext.createPoller(0);

        final ZMQ.Socket control = this.zContext.createSocket(REP);
        for (final String addr : controlAddresses) control.bind(addr);

        final ZMQ.Socket invoker = this.zContext.createSocket(ROUTER);
        for (final String addr : invokerAddresses) invoker.bind(addr);

        this.multiplex = new JeroMQMultiplexRouter(poller);

        final int invokerIndex = poller.register(invoker, POLLIN | POLLERR);
        this.demultiplex = new JeroMQDemultiplexRouter(poller, invokerIndex);

        final int controlIndex = poller.register(control, POLLIN | POLLERR);
        this.control = new JeroMQControlServer(instanceId, poller, controlIndex, multiplex, demultiplex);

    }

    public void run() {
        while (!interrupted()) {

            if (poller.poll(POLL_TIMEOUT_MILLISECONDS) < 0) {
                logger.info("Poller signaled interruption.  Exiting.");
                break;
            }

            control.poll();
            multiplex.poll();
            demultiplex.poll();

        }
    }

    @Override
    public void close() {
        poller.close();
        zContext.close();
    }

}
