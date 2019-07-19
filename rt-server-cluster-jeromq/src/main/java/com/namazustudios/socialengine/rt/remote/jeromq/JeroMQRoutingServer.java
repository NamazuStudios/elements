package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.charset.Charset;
import java.util.List;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlException.exceptionError;
import static java.lang.Thread.interrupted;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQRoutingServer implements AutoCloseable {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRoutingServer.class);

    private static final long POLL_TIMEOUT_MILLISECONDS = 5000;

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final int main;

    private final JeroMQControlServer control;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    public JeroMQRoutingServer(final InstanceId instanceId,
                               final ZContext zContext,
                               final List<String> bindAddresses) {

        this.zContext = shadow(zContext);
        this.poller = zContext.createPoller(0);

        final ZMQ.Socket main = this.zContext.createSocket(ROUTER);
        for (final String addr : bindAddresses) main.bind(addr);

        this.main = poller.register(main, POLLIN | POLLERR);
        this.multiplex = new JeroMQMultiplexRouter(this.zContext, poller);
        this.demultiplex = new JeroMQDemultiplexRouter(poller, this.main);
        this.control = new JeroMQControlServer(instanceId, multiplex, demultiplex);

    }

    public void run() {
        while (!interrupted()) {

            if (poller.poll(POLL_TIMEOUT_MILLISECONDS) < 0) {
                logger.info("Poller signaled interruption.  Exiting.");
                break;
            }

            try {

                if (poller.pollin(main)) {
                    dispatchMain();
                }

                multiplex.poll();
                demultiplex.poll();

            } catch (Exception ex) {
                logger.error("Caught exception in routing server.", ex);
            }

        }

    }

    private void dispatchMain() {

        final ZMQ.Socket socket = poller.getSocket(main);
        final ZMsg zMsg = ZMsg.recvMsg(socket);
        final ZMsg identity = popIdentity(zMsg);

        final JeroMQControlCommand command;

        try {
            command = JeroMQControlCommand.stripCommand(zMsg);
        } catch (IllegalArgumentException ex) {
            final ZMsg response = exceptionError(ex);
            pushIdentity(response, identity);
            response.send(socket);
            return;
        }

        final boolean handled = control.handle(socket, zMsg, command, identity) ||
                                demultiplex.handle(socket, zMsg, command, identity);

        if (handled && logger.isTraceEnabled()) {
            logger.trace("Handled incoming message {}", zMsg);
        } else if (!handled) {
            logger.error("Dropping message from main socket: {}", zMsg);
        }

    }

    @Override
    public void close() {
        poller.close();
        zContext.close();
    }

}
