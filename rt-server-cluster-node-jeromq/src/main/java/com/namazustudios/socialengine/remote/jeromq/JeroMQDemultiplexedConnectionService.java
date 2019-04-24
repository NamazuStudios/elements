package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import com.namazustudios.socialengine.rt.remote.srv.SrvMonitor;
import com.namazustudios.socialengine.rt.remote.srv.SrvRecord;
import com.namazustudios.socialengine.rt.remote.srv.SrvUniqueIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder.buildControlMsg;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;

public class JeroMQDemultiplexedConnectionService implements ConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQDemultiplexedConnectionService.class);

    public static final String BIND_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQDemultiplexedConnectionService.bindAddress";
    public static final String CONTROL_BIND_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQDemultiplexedConnectionService.controlBindAddress";
    public static final String APPLICATION_NODE_FQDN = "com.namazustudios.socialengine.remote.jeromq.JeroMQDemultiplexedConnectionService.applicationNodeFqdn";

    private String bindAddress;
    private String controlBindAddress;
    private String applicationNodeFqdn;

    private final AtomicReference<Thread> atomicDemultiplexedConnectionThread = new AtomicReference<>();

    private ZContext zContext;

    private final String controlAddress = format("inproc://%s.control", randomUUID());

    // TODO: move srv monitor stuff to common location, utilized by both multiplexed and demultiplexed services
    private SrvMonitor srvMonitor;

    @Override
    public void start() {
        setUpAndStartDemultiplexedConnection();
        setUpAndStartSrvMonitor();
    }

    private void setUpAndStartDemultiplexedConnection() {
        final Set<String> controlAddresses = new HashSet<>();
        controlAddresses.add(controlAddress);
        controlAddresses.add(controlBindAddress);
        final JeroMQDemultiplexedConnectionRunnable demultiplexedConnectionRunnable = new JeroMQDemultiplexedConnectionRunnable(
                controlAddresses,
                getzContext()
        );
        final Thread demultiplexedConnectionThread = new Thread(demultiplexedConnectionRunnable);

        demultiplexedConnectionThread.setDaemon(true);
        demultiplexedConnectionThread.setName(JeroMQDemultiplexedConnectionService.class.getSimpleName());
        demultiplexedConnectionThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

        if (atomicDemultiplexedConnectionThread.compareAndSet(null, demultiplexedConnectionThread)) {
            logger.info("Starting demultiplexed thread and establishing control channel....");
            demultiplexedConnectionThread.start();
            demultiplexedConnectionRunnable.blockCurrentThreadUntilControlChannelIsConnected();
            logger.info("Successfully started demultiplexed thread and established control channel.");
        } else {
            throw new IllegalStateException("Failed to set up demultiplexed connection.");
        }
    }

    void setUpAndStartSrvMonitor() {

        srvMonitor.registerOnCreatedSrvRecordListener((SrvRecord srvRecord) -> {
            // TODO: need a way to ignore current node's SRV record
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

    @Override
    public void stop() {

        final Thread demultiplexedConnectionManagerThread = atomicDemultiplexedConnectionThread.get();

        if (atomicDemultiplexedConnectionThread.compareAndSet(demultiplexedConnectionManagerThread, null)) {
            demultiplexedConnectionManagerThread.interrupt();

            try {
                demultiplexedConnectionManagerThread.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Failed to tear down demultiplexed connection.");
            }
        }

    }

    @Override
    public void issueCommand(final CommandType commandType, final ByteBuffer byteBuffer) {
        try (final ZContext context = shadow(getzContext());
             final Connection connection = from(context, c -> c.createSocket(REQ))) {
            connection.socket().connect(getControlAddress());

            final ZMsg msg = buildControlMsg(commandType, byteBuffer);

            connection.sendMessage(msg);
            connection.socket().recv();
        }
    }

    @Override
    // TODO: make some intermediary connection service that takes care of this and srv monitor
    public boolean connectToBackend(final SrvUniqueIdentifier srvUniqueIdentifier) {
        final String backendAddress = RouteRepresentationUtil.buildBackendAddress(
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
        final String backendAddress = RouteRepresentationUtil.buildBackendAddress(
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

    public String getBindAddress() {
        return bindAddress;
    }

    @Inject
    public void setBindAddress(@Named(BIND_ADDR) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public String getControlBindAddress() {
        return controlBindAddress;
    }

    @Inject
    public void setControlBindAddress(@Named(CONTROL_BIND_ADDR) String controlBindAddress) {
        this.controlBindAddress = controlBindAddress;
    }

    public String getControlAddress() {
        return controlAddress;
    }

    public String getApplicationNodeFqdn() {
        return applicationNodeFqdn;
    }

    @Inject
    public void setApplicationNodeFqdn(@Named(APPLICATION_NODE_FQDN) String applicationNodeFqdn) {
        this.applicationNodeFqdn = applicationNodeFqdn;
    }

    public SrvMonitor getSrvMonitor() {
        return srvMonitor;
    }

    @Inject
    public void setSrvMonitor(SrvMonitor srvMonitor) {
        this.srvMonitor = srvMonitor;
    }

}
