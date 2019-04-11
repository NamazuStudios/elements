package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.remote.jeromq.srv.SrvMonitor;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvRecord;
import com.namazustudios.socialengine.remote.jeromq.srv.SrvUniqueIdentifier;
import com.namazustudios.socialengine.rt.MultiplexedConnectionManager;
import com.namazustudios.socialengine.rt.jeromq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.*;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost.send;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.*;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;

public class JeroMQMultiplexedConnectionManager implements MultiplexedConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexedConnectionManager.class);

    public static final String CONNECT_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.connectAddress";
    public static final String APPLICATION_NODE_FQDN = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.applicationNodeFqdn";

    private final AtomicReference<Thread> atomicMultiplexedConnectionThread = new AtomicReference<>();

    private Routing routing;

    private ZContext zContext;

    private String applicationNodeFqdn;

    private final String controlAddress = format("inproc://%s.control", randomUUID());

    private SrvMonitor srvMonitor;

    @Override
    public void start() {

        setupAndStartMultiplexedConnection();
        setupAndStartSrvMonitor();

    }

    private void setupAndStartMultiplexedConnection() {
        final JeroMQMultiplexedConnection multiplexedConnection = new JeroMQMultiplexedConnection(
                controlAddress,
                getzContext(),
                getRouting()
        );
        final Thread multiplexedConnectionManagerThread = new Thread(multiplexedConnection);

        multiplexedConnectionManagerThread.setDaemon(true);
        multiplexedConnectionManagerThread.setName(JeroMQMultiplexedConnectionManager.class.getSimpleName());
        multiplexedConnectionManagerThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

        if (atomicMultiplexedConnectionThread.compareAndSet(null, multiplexedConnectionManagerThread)) {
            multiplexedConnectionManagerThread.start();
            multiplexedConnection.waitForConnect();
        } else {
            throw new IllegalStateException("Failed to set up multiplexed connection.");
        }
    }

    private void setupAndStartSrvMonitor() {

        srvMonitor.registerOnCreatedSrvRecordListener((SrvRecord srvRecord) -> {
            logger.info("Detected App Node SRV record creation: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            final boolean didIssueCommand = connectToBackend(srvRecord.getUniqueIdentifier());

            if (didIssueCommand) {
                logger.info("Successfully issued open backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            }
            else {
                logger.info("Failed to issue open backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            }
        });

        srvMonitor.registerOnUpdatedSrvRecordListener((SrvRecord srvRecord) -> {
            // for now, ignore updates
            logger.info("Detected App Node SRV record update: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
        });

        srvMonitor.registerOnDeletedSrvRecordListener((SrvRecord srvRecord) -> {
            logger.info("Detected App Node SRV record deletion: host={} port={}",
                    srvRecord.getHost(), srvRecord.getPort());

            final boolean didIssueCommand = disconnectFromBackend(srvRecord.getUniqueIdentifier());

            if (didIssueCommand) {
                logger.info("Successfully issued close backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            }
            else {
                logger.info("Failed to issue close backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            }
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

    private boolean connectToBackend(final SrvUniqueIdentifier srvUniqueIdentifier) {
        final String backendAddress = buildBackendAddress(srvUniqueIdentifier);
        if (backendAddress == null) {
            return false;
        }

        openBackendChannel(backendAddress);

        return true;
    }

    private static boolean isLocalhost(final SrvUniqueIdentifier srvUniqueIdentifier) {
        try {
            String localHost = InetAddress.getLocalHost().getHostName();
            if (!localHost.endsWith(".")) {
                localHost = localHost + ".";
            }

            if (srvUniqueIdentifier.getHost().equals(localHost)) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (UnknownHostException e) {
            // TODO: determine best strategy to handle this
            return false;
        }
    }

    private static String buildBackendAddress(final SrvUniqueIdentifier srvUniqueIdentifier) {
        final String host = srvUniqueIdentifier.getHost();
        final int port = srvUniqueIdentifier.getPort();

        if (host == null || host.length() == 0 || port < 0) {
            return null;
        }

        final String backendAddress = "tcp://" + host.substring(0, host.length() - 1) + ":" + port;

        return backendAddress;
    }

    private boolean disconnectFromBackend(final SrvUniqueIdentifier srvUniqueIdentifier) {
        final String connectAddress = buildBackendAddress(srvUniqueIdentifier);
        if (connectAddress == null) {
            return false;
        }

        closeBackendChannel(connectAddress);

        return true;
    }

    @Override
    public void stop() {

        final Thread multiplexedConnectionManagerThread = atomicMultiplexedConnectionThread.get();

        if (atomicMultiplexedConnectionThread.compareAndSet(multiplexedConnectionManagerThread, null)) {
            multiplexedConnectionManagerThread.interrupt();

            try {
                multiplexedConnectionManagerThread.join();
            } catch (InterruptedException e) {

            }
        }

    }

    @Override
    public UUID getInprocIdentifierForNodeIdentifier(final String destinationNodeId) {
        return getRouting().getDestinationId(destinationNodeId);
    }

    @Override
    public String getInprocConnectAddress(final UUID inprocIdentifier) {
        return getRouting().getMultiplexedAddressForDestinationId(inprocIdentifier);
    }

    @Override
    public void openInprocChannel(final String backendAddress, final UUID inprocIdentifier) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(OPEN_INPROC);
        command.backendAddress.set(backendAddress);
        command.inprocIdentifier.set(inprocIdentifier);
        issue(command);
    }

    @Override
    public void closeInprocChannel(final String backendAddress, final UUID inprocIdentifier) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(CLOSE_INPROC);
        command.backendAddress.set(backendAddress);
        command.inprocIdentifier.set(inprocIdentifier);
        issue(command);
    }

    @Override
    public void openBackendChannel(final String backendAddress) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(OPEN_BACKEND);
        command.backendAddress.set(backendAddress);
        issue(command);
    }

    @Override
    public void closeBackendChannel(final String backendAddress) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(CLOSE_BACKEND);
        command.backendAddress.set(backendAddress);
        issue(command);
    }

    private void issue(final RoutingCommand command) {
        issue(ROUTING_COMMAND, command.getByteBuffer());
    }

    private void issue(final CommandType commandType, final ByteBuffer byteBuffer) {
        try (final ZContext context = shadow(getzContext());
             final Connection connection = from(context, c -> c.createSocket(PUSH))) {
            connection.socket().connect(getControlAddress());
            send(connection.socket(), commandType, byteBuffer);
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
