package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.remote.jeromq.srv.SrvMonitor;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvRecord;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvUniqueIdentifier;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.SRVRecord;
import org.zeromq.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.ROUTING_COMMAND;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost.send;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.CLOSE;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.OPEN;
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.CONTINUE;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMsg.recvMsg;
import static zmq.ZError.EHOSTUNREACH;

public class JeroMQConnectionMultiplexer implements ConnectionMultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionMultiplexer.class);

    public static final String CONNECT_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.connectAddress";
    public static final String APPLICATION_NODE_FQDN = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.applicationNodeFqdn";

    private final Map<SrvUniqueIdentifier, AtomicReference<Thread>> atomicMultiplexedConnectionManagerThreads = new HashMap<>();

    private Routing routing;

    private ZContext zContext;

    private String connectAddress;

    private String applicationNodeFqdn;

    private final String controlAddress = format("inproc://%s.control", randomUUID());

    private SrvMonitor srvMonitor;

    @Override
    public void start() {

        setupAndStartSrvMonitor();

    }

    private void setupAndStartSrvMonitor() {

        srvMonitor.registerOnCreatedSrvRecordListener((SrvRecord srvRecord) -> {
            logger.info("Detected App Node SRV record creation: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            final boolean didStart = createAndStartMultiplexedConnectionManagerIfNecessary(srvRecord.getUniqueIdentifier());

            if (didStart) {
                logger.info("Successfully started MultiplexedConnectionManager for SRV record: host={} port={}",
                        srvRecord.getHost(), srvRecord.getPort());
            }
            else {
                logger.info("Failed to start MultiplexedConnectionManager for SRV record: host={} port={}",
                        srvRecord.getHost(), srvRecord.getPort());
            }
        });

        srvMonitor.registerOnUpdatedSrvRecordListener((SrvRecord srvRecord) -> {
            // for now, ignore updates
        });

        srvMonitor.registerOnDeletedSrvRecordListener((SrvRecord srvRecord) -> {
            logger.info("Detected App Node SRV record deletion: host={} port={}",
                    srvRecord.getHost(), srvRecord.getPort());
            stopAndDeleteMultiplexedConnectionManagerIfPossible(srvRecord.getUniqueIdentifier());
        });

        logger.info("Starting SRV record monitor for FQDN: {}...", applicationNodeFqdn);
        final boolean didStart = srvMonitor.start(applicationNodeFqdn);

        if (didStart) {
            logger.info("Successfully started SRV record monitor for FQDN: {}", applicationNodeFqdn);
        }
        else {
            throw new IllegalStateException("Failed to start SRV record monitor for FQDN: " + applicationNodeFqdn);
        }

    }

    private boolean createAndStartMultiplexedConnectionManagerIfNecessary(SrvUniqueIdentifier srvUniqueIdentifier) {

        if (atomicMultiplexedConnectionManagerThreads.containsKey(srvUniqueIdentifier)) {
            return true;
        }

        final AtomicReference<Thread> atomicThreadReference = new AtomicReference<>();

        final MultiplexedConnectionManager multiplexedConnectionManager = new MultiplexedConnectionManager();
        final Thread multiplexedConnectionManagerThread = new Thread(multiplexedConnectionManager);

        multiplexedConnectionManagerThread.setDaemon(true);
        multiplexedConnectionManagerThread.setName(JeroMQConnectionMultiplexer.class.getSimpleName());
        multiplexedConnectionManagerThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

        if (atomicThreadReference.compareAndSet(null, multiplexedConnectionManagerThread)) {
            multiplexedConnectionManagerThread.start();
            multiplexedConnectionManager.waitForConnect();
            atomicMultiplexedConnectionManagerThreads.put(srvUniqueIdentifier, atomicThreadReference);

            return true;
        } else {
            return false;
        }

    }

    private void stopAndDeleteMultiplexedConnectionManagerIfPossible(SrvUniqueIdentifier srvUniqueIdentifier) {
        final boolean didStop = stopMultiplexedConnectionManagerIfPossible(srvUniqueIdentifier);

        if (didStop) {
            atomicMultiplexedConnectionManagerThreads.remove(srvUniqueIdentifier);
        }
    }

    private boolean stopMultiplexedConnectionManagerIfPossible(SrvUniqueIdentifier srvUniqueIdentifier) {
        if (!atomicMultiplexedConnectionManagerThreads.containsKey(srvUniqueIdentifier)) {
            return true;
        }

        final AtomicReference<Thread> atomicThreadReference = atomicMultiplexedConnectionManagerThreads.get(srvUniqueIdentifier);

        final Thread multiplexedConnectionManagerThread = atomicThreadReference.get();

        if (atomicThreadReference.compareAndSet(multiplexedConnectionManagerThread, null)) {
            multiplexedConnectionManagerThread.interrupt();

            try {
                multiplexedConnectionManagerThread.join();
                return true;
            }
            catch (InterruptedException e) {
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public void stop() {

        atomicMultiplexedConnectionManagerThreads
                .keySet()
                .stream()
                .forEach(this::stopMultiplexedConnectionManagerIfPossible);

        atomicMultiplexedConnectionManagerThreads.clear();

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

    public String getApplicationNodeFqdn() {
        return applicationNodeFqdn;
    }

    @Inject
    public void setApplicationNodeFqdn(@Named(APPLICATION_NODE_FQDN) String applicationNodeFqdn) {
        this.applicationNodeFqdn = applicationNodeFqdn;
    }

    public String getControlAddress() {
        return controlAddress;
    }

    public SrvMonitor getSrvMonitor() {
        return srvMonitor;
    }

    @Inject
    public void setSrvMonitor(SrvMonitor srvMonitor) {
        this.srvMonitor = srvMonitor;
    }

    /**
     * Threaded manager for a multiplexed ZMQ connection.
     */
    private class MultiplexedConnectionManager implements Runnable {

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
                    backend.socket().connect(getConnectAddress());
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
