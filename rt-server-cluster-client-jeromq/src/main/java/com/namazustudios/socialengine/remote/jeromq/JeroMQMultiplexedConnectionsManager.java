package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.remote.jeromq.srv.SrvMonitor;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvRecord;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvUniqueIdentifier;
import com.namazustudios.socialengine.rt.MultiplexedConnectionsManager;
import com.namazustudios.socialengine.rt.jeromq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.ROUTING_COMMAND;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost.send;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.CLOSE;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.OPEN;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;

public class JeroMQMultiplexedConnectionsManager implements MultiplexedConnectionsManager {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexedConnectionsManager.class);

    public static final String CONNECT_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.connectAddress";
    public static final String APPLICATION_NODE_FQDN = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.applicationNodeFqdn";

    private final Map<SrvUniqueIdentifier, AtomicReference<Thread>> atomicMultiplexedConnectionThreads = new HashMap<>();

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

        if (atomicMultiplexedConnectionThreads.containsKey(srvUniqueIdentifier)) {
            return true;
        }

        final AtomicReference<Thread> atomicThreadReference = new AtomicReference<>();

        final String connectAddress = buildConnectAddress(srvUniqueIdentifier);
        if (connectAddress == null) {
            return false;
        }

        final JeroMQMultiplexedConnection multiplexedConnection = new JeroMQMultiplexedConnection(connectAddress, controlAddress);
        final Thread multiplexedConnectionManagerThread = new Thread(multiplexedConnection);

        multiplexedConnectionManagerThread.setDaemon(true);
        multiplexedConnectionManagerThread.setName(JeroMQMultiplexedConnectionsManager.class.getSimpleName());
        multiplexedConnectionManagerThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

        if (atomicThreadReference.compareAndSet(null, multiplexedConnectionManagerThread)) {
            multiplexedConnectionManagerThread.start();
            multiplexedConnection.waitForConnect();
            atomicMultiplexedConnectionThreads.put(srvUniqueIdentifier, atomicThreadReference);

            return true;
        } else {
            return false;
        }

    }

    private static String buildConnectAddress(SrvUniqueIdentifier srvUniqueIdentifier) {
        final String host = srvUniqueIdentifier.getHost();
        final int port = srvUniqueIdentifier.getPort();

        if (host == null || host.length() == 0 || port < 0) {
            return null;
        }

        final String connectAddress = "tcp://" + host.substring(0, host.length() - 1) + ":" + port;

        return connectAddress;
    }

    private void stopAndDeleteMultiplexedConnectionManagerIfPossible(SrvUniqueIdentifier srvUniqueIdentifier) {
        final boolean didStop = stopMultiplexedConnectionManagerIfPossible(srvUniqueIdentifier);

        if (didStop) {
            atomicMultiplexedConnectionThreads.remove(srvUniqueIdentifier);
        }
    }

    private boolean stopMultiplexedConnectionManagerIfPossible(SrvUniqueIdentifier srvUniqueIdentifier) {
        if (!atomicMultiplexedConnectionThreads.containsKey(srvUniqueIdentifier)) {
            return true;
        }

        final AtomicReference<Thread> atomicThreadReference = atomicMultiplexedConnectionThreads.get(srvUniqueIdentifier);

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

        atomicMultiplexedConnectionThreads
                .keySet()
                .stream()
                .forEach(this::stopMultiplexedConnectionManagerIfPossible);

        atomicMultiplexedConnectionThreads.clear();

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

}
