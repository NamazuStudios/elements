package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.srv.SrvMonitorService;
import com.namazustudios.socialengine.rt.srv.SrvRecord;
import com.namazustudios.socialengine.rt.SrvUniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import com.namazustudios.socialengine.rt.remote.ConnectionService;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder.buildControlMsg;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;

public class JeroMQMultiplexedConnectionService implements ConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexedConnectionService.class);

    public static final String APPLICATION_NODE_FQDN = "com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService.applicationNodeFqdn";

    private String applicationNodeFqdn;

    private final AtomicReference<Thread> atomicMultiplexedConnectionThread = new AtomicReference<>();

    private ZContext zContext;

    private final String controlAddress = format("inproc://%s.control", randomUUID());

    private SrvMonitorService srvMonitorService;

    @Override
    public void start() {

        setUpAndStartMultiplexedConnection();
        setUpAndStartSrvMonitor();

    }

    private void setUpAndStartMultiplexedConnection() {
        final JeroMQMultiplexedConnectionRunnable multiplexedConnectionRunnable = new JeroMQMultiplexedConnectionRunnable(
                controlAddress,
                getzContext()
        );
        final Thread multiplexedConnectionThread = new Thread(multiplexedConnectionRunnable);

        multiplexedConnectionThread.setDaemon(true);
        multiplexedConnectionThread.setName(JeroMQMultiplexedConnectionService.class.getSimpleName());
        multiplexedConnectionThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

        if (atomicMultiplexedConnectionThread.compareAndSet(null, multiplexedConnectionThread)) {
            logger.info("Starting multiplexed thread and establishing control channel....");
            multiplexedConnectionThread.start();
            multiplexedConnectionRunnable.blockCurrentThreadUntilControlChannelIsConnected();
            logger.info("Successfully started multiplexed thread and established control channel.");
        } else {
            throw new IllegalStateException("Failed to set up multiplexed connection.");
        }
    }

    private void setUpAndStartSrvMonitor() {

        srvMonitorService.registerOnCreatedSrvRecordListener((SrvRecord srvRecord) -> {
            logger.info("Detected App Node SRV record creation: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            final boolean didIssueCommand = connectToBackend(srvRecord.getUniqueIdentifier());

            if (didIssueCommand) {
                logger.info("Successfully issued open backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            }
            else {
                logger.info("Failed to issue open backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
            }
        });

        srvMonitorService.registerOnUpdatedSrvRecordListener((SrvRecord srvRecord) -> {
            // for now, ignore updates
            logger.info("Detected App Node SRV record update: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
        });

        srvMonitorService.registerOnDeletedSrvRecordListener((SrvRecord srvRecord) -> {
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
        final boolean didStart = srvMonitorService.start(applicationNodeFqdn);

        if (didStart) {
            logger.info("Successfully started SRV record monitor for FQDN: {}", applicationNodeFqdn);
        }
        else {
            throw new IllegalStateException("Failed to start SRV record monitor for FQDN: " + applicationNodeFqdn);
        }

    }

    @Override
    public void stop() {

        final Thread multiplexedConnectionManagerThread = atomicMultiplexedConnectionThread.get();

        if (atomicMultiplexedConnectionThread.compareAndSet(multiplexedConnectionManagerThread, null)) {
            multiplexedConnectionManagerThread.interrupt();

            try {
                multiplexedConnectionManagerThread.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Failed to tear down multiplexed connection.");
            }
        }

    }

    @Override
    public void issueCommand(final CommandType commandType, final ByteBuffer byteBuffer) {
        try (final ZContext context = shadow(getzContext());
             final Connection connection = from(context, c -> c.createSocket(PUSH))) {
            connection.socket().connect(getControlAddress());

            final ZMsg msg = buildControlMsg(commandType, byteBuffer);

            connection.sendMessage(msg);
        }
    }

    @Override
    // TODO: make some intermediary connection service that takes care of this and srv monitor
    public boolean connectToBackend(final SrvUniqueIdentifier srvUniqueIdentifier) {
        final String backendAddress = RouteRepresentationUtil.buildTcpAddress(
                srvUniqueIdentifier.getHost(),
                srvUniqueIdentifier.getPort());

        if (backendAddress == null) {
            return false;
        }

        issueConnectTcpCommand(backendAddress);

        return true;
    }

    @Override
    public boolean disconnectFromBackend(final SrvUniqueIdentifier srvUniqueIdentifier) {
        final String backendAddress = RouteRepresentationUtil.buildTcpAddress(
                srvUniqueIdentifier.getHost(),
                srvUniqueIdentifier.getPort());

        if (backendAddress == null) {
            return false;
        }

        issueDisconnectTcpCommand(backendAddress);

        return true;
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

    public SrvMonitorService getSrvMonitorService() {
        return srvMonitorService;
    }

    @Inject
    public void setSrvMonitorService(SrvMonitorService srvMonitorService) {
        this.srvMonitorService = srvMonitorService;
    }

}
