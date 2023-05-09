    package dev.getelements.elements.remote.jeromq;

    import dev.getelements.elements.rt.ConnectionMultiplexer;
    import dev.getelements.elements.rt.exception.InternalException;
    import dev.getelements.elements.rt.jeromq.*;
    import dev.getelements.elements.rt.remote.RoutingHeader;
    import dev.getelements.elements.rt.util.SyncWait;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.zeromq.*;

    import javax.inject.Inject;
    import javax.inject.Named;
    import java.net.InetAddress;
    import java.net.UnknownHostException;
    import java.util.Optional;
    import java.util.UUID;
    import java.util.concurrent.atomic.AtomicReference;
    import java.util.regex.Pattern;
    import java.util.stream.Stream;

    import static dev.getelements.elements.rt.jeromq.CommandPreamble.CommandType.ROUTING_COMMAND;
    import static dev.getelements.elements.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
    import static dev.getelements.elements.rt.jeromq.Connection.from;
    import static dev.getelements.elements.rt.jeromq.JeroMQSocketHost.send;
    import static dev.getelements.elements.rt.jeromq.RoutingCommand.Action.CLOSE;
    import static dev.getelements.elements.rt.jeromq.RoutingCommand.Action.OPEN;
    import static dev.getelements.elements.rt.remote.RoutingHeader.Status.CONTINUE;
    import static java.lang.String.format;
    import static java.lang.Thread.interrupted;
    import static java.util.UUID.randomUUID;
    import static java.util.concurrent.TimeUnit.MILLISECONDS;
    import static java.util.concurrent.TimeUnit.SECONDS;
    import static java.util.stream.IntStream.range;
    import static org.zeromq.ZContext.shadow;
    import static org.zeromq.ZMQ.*;
    import static org.zeromq.ZMQ.Poller.POLLERR;
    import static org.zeromq.ZMQ.Poller.POLLIN;
    import static org.zeromq.ZMsg.recvMsg;
    import static zmq.ZError.EHOSTUNREACH;

public class JeroMQConnectionMultiplexer implements ConnectionMultiplexer {

    private static final long RESOLVE_TIME = MILLISECONDS.convert(5, SECONDS);

    private static final long RESOLVE_RETRY_ATTEMPTS = 60;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionMultiplexer.class);

    public static final String CONNECT_ADDR = "dev.getelements.elements.remote.jeromq.JeroMQConnectionMultiplexer.connectAddress";

    private final AtomicReference<Thread> multiplexerThread = new AtomicReference<>();

    private Routing routing;

    private ZContext zContext;

    private String connectAddress;

    private final String controlAddress = format("inproc://%s.control", randomUUID());

    @Override
    public void start() {

        final Multiplexer multiplexer = new Multiplexer();
        final Thread thread = new Thread(multiplexer);

        thread.setDaemon(true);
        thread.setName(JeroMQConnectionMultiplexer.class.getSimpleName());
        thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

        if (multiplexerThread.compareAndSet(null, thread)) {
            thread.start();
            multiplexer.waitForConnect();
        } else {
            throw new IllegalStateException("Multiplexer already started.");
        }

    }

    @Override
    public void stop() {

        final Thread thread = multiplexerThread.get();

        if (multiplexerThread.compareAndSet(thread, null)) {

            thread.interrupt();

            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new InternalException("Interrupted while shutting down the connection router.", ex);
            }

        } else {
            throw new IllegalStateException("Multiplexer already started.");
        }

    }

    @Override
    public UUID getDestinationUUIDForNodeId(String destinationNodeId) {
        return getRouting().getDestinationId(destinationNodeId);
    }

    @Override
    public String getConnectAddress(UUID uuid) {
        return getRouting().getMultiplexedAddressForDestinationId(uuid);
    }

    @Override
    public void open(UUID destination) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(OPEN);
        command.destination.set(destination);
        issue(command);
    }

    @Override
    public void close(UUID destination) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(CLOSE);
        command.destination.set(destination);
        issue(command);
    }

    private void issue(final RoutingCommand command) {
        try (final ZContext context = shadow(getzContext());
             final Connection connection = from(context, c -> c.createSocket(PUSH))) {
            connection.socket().connect(getControlAddress());
            send(connection.socket(), ROUTING_COMMAND, command.getByteBuffer());
        }
    }

    public Routing getRouting() {
        return routing;
    }

    @Inject
    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public String getConnectAddress() {
        return connectAddress;
    }

    @Inject
    public void setConnectAddress(@Named(CONNECT_ADDR) String connectAddress) {
        this.connectAddress = connectAddress;
    }

    public String getControlAddress() {
        return controlAddress;
    }

    private class Multiplexer implements Runnable {

        private final SyncWait<Void> connectSyncWait = new SyncWait<Void>(logger);

        public void waitForConnect() {
            connectSyncWait.get();
        }

        @Override
        public void run() {

            try (final ZContext context = shadow(getzContext());
                 final ZMQ.Poller poller = context.createPoller(0);
                 final Connection backend = from(context, c -> c.createSocket(DEALER));
                 final Connection control = from(context, c -> c.createSocket(PULL));
                 final RoutingTable frontends = new RoutingTable(context, poller, uuid -> bind(context, uuid));
                 final MonitorThread monitorThread = new MonitorThread(getClass().getSimpleName(), logger, context, backend.socket())) {

                final int backendIndex;
                final int controlIndex;

                try {
                    monitorThread.start();
                    resolveAndConnect(backend.socket(), getConnectAddress());
                    control.socket().bind(getControlAddress());
                    backendIndex = poller.register(backend.socket(), POLLIN | POLLERR);
                    controlIndex = poller.register(control.socket(), POLLIN | POLLERR);
                    connectSyncWait.getResultConsumer().accept(null);
                } catch (Exception ex) {
                    connectSyncWait.getErrorConsumer().accept(ex);
                    return;
                }

                while (!interrupted()) {

                    if (poller.poll(5000) < 0) {
                        logger.info("Interrupted.  Exiting gracefully.");
                        break;
                    }

                    range(0, poller.getNext()).filter(index -> poller.getItem(index) != null).forEach(index -> {

                        final boolean input = poller.pollin(index);
                        final boolean error = poller.pollerr(index);

                        if (input) {
                            if (index == backendIndex) {
                                sendToFrontend(poller, index, frontends);
                            } else if (index == controlIndex) {
                                handleControlMessage(control.socket(), frontends);
                            } else {
                                sendToBackend(poller, index, frontends, backend.socket());
                            }
                        } else if (error) {
                            throw new InternalException("Poller error on socket: " + poller.getSocket(index));
                        }

                    });

                }

            }

        }

        private void resolveAndConnect(final Socket socket, final String host) throws Exception {
            for (int attempt = 0; attempt < RESOLVE_RETRY_ATTEMPTS && !interrupted(); ++attempt) {
                try {
                    socket.connect(host);
                } catch (ZMQException ex) {
                    if (ex.getCause() instanceof UnknownHostException) {
                        logger.info("Couldn't find host {}. Attempting again in {}ms.", host, RESOLVE_TIME);
                        Thread.sleep(RESOLVE_TIME);
                    } else {
                        throw ex;
                    }
                }
            }
        }

        private ZMQ.Socket bind(final ZContext context, final UUID uuid) {
            final ZMQ.Socket socket = context.createSocket(ROUTER);
            final String bindAddress = getRouting().getMultiplexedAddressForDestinationId(uuid);
            socket.setRouterMandatory(true);
            socket.bind(bindAddress);
            return socket;
        }

        private void sendToFrontend(final ZMQ.Poller poller, final int index, final RoutingTable frontends) {
            final ZMQ.Socket socket = poller.getSocket(index);
            final ZMsg msg = recvMsg(socket);

            final RoutingHeader routingHeader = getRouting().stripRoutingHeader(msg);

            if (routingHeader.status.get() == CONTINUE) {

                final UUID destination = routingHeader.destination.get();
                final ZMQ.Socket frontend = frontends.getSocket(destination);

                try {
                    msg.send(frontend);
                } catch (ZMQException ex) {
                    if (ex.getErrorCode() == EHOSTUNREACH) {
                        logger.warn("Host unreachable.  Dropping message.");
                    } else {
                        throw ex;
                    }
                }

            } else {
                logger.error("Received {} route for destination {}", routingHeader.status.get(), routingHeader.destination.get());
            }

        }

        private void sendToBackend(final ZMQ.Poller poller, final int index,
                                   final RoutingTable frontends, final ZMQ.Socket backend) {

            final ZMQ.Socket socket = poller.getSocket(index);
            final ZMsg msg = recvMsg(socket);
            final UUID destination = frontends.getDestination(index);

            final RoutingHeader routingHeader = new RoutingHeader();
            routingHeader.status.set(CONTINUE);
            routingHeader.destination.set(destination);

            getRouting().insertRoutingHeader(msg, routingHeader);
            msg.send(backend);

        }

        private void handleControlMessage(final ZMQ.Socket control, final RoutingTable frontends) {

            final ZMsg msg = ZMsg.recvMsg(control);
            final CommandPreamble preamble = new CommandPreamble();

            preamble.getByteBuffer().put(msg.pop().getData());

            switch(preamble.commandType.get()) {
                case STATUS_REQUEST:
                    send(control, STATUS_RESPONSE, new StatusResponse().getByteBuffer());
                    break;
                case ROUTING_COMMAND:
                    final RoutingCommand command = new RoutingCommand();
                    command.getByteBuffer().put(msg.pop().getData());
                    frontends.process(command);
                    break;
                default:
                    logger.error("Unexpected command: {}", preamble.commandType.get());
            }

        }

    }

}
