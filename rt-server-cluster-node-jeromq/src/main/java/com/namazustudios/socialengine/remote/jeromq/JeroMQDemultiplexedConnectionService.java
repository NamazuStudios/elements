//package com.namazustudios.socialengine.remote.jeromq;
//
//import com.google.common.net.HostAndPort;
//import com.namazustudios.socialengine.rt.jeromq.*;
//import com.namazustudios.socialengine.rt.remote.ConnectionService;
//import com.namazustudios.socialengine.rt.srv.SrvMonitorService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.zeromq.*;
//
//import javax.inject.Inject;
//import javax.inject.Named;
//import java.nio.ByteBuffer;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static com.namazustudios.socialengine.rt.Constants.*;
//import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
//import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
//import static com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder.buildControlMsg;
//import static java.lang.String.format;
//import static java.util.UUID.randomUUID;
//import static org.zeromq.ZContext.shadow;
//import static org.zeromq.ZMQ.*;
//
//public class JeroMQDemultiplexedConnectionService implements ConnectionService {
//
//    private static final Logger logger = LoggerFactory.getLogger(JeroMQDemultiplexedConnectionService.class);
//
//    public static final String APPLICATION_NODE_FQDN = "com.namazustudios.socialengine.remote.jeromq.JeroMQDemultiplexedConnectionService.applicationNodeFqdn";
//
//    private Integer currentInstanceInvokerPort;
//    private Integer currentInstanceControlPort;
//    private String applicationNodeFqdn;
//
//    private UUID instanceUuid;
//
//    private final AtomicReference<Thread> atomicDemultiplexedConnectionThread = new AtomicReference<>();
//
//    private ZContext zContext;
//
//    private final String controlAddress = format("inproc://%s.control", randomUUID());
//
//    // TODO: move srv monitor stuff to common location, utilized by both multiplexed and demultiplexed services
//    private SrvMonitorService srvMonitorService;
//
//    @Override
//    public void start() {
//        setUpAndStartDemultiplexedConnection();
//        setUpAndStartSrvMonitor();
//    }
//
//    private void setUpAndStartDemultiplexedConnection() {
//        final Set<String> controlAddresses = new HashSet<>();
//        controlAddresses.add(controlAddress);
//        final String controlBindAddress = RouteRepresentationUtil.buildTcpAddress("*", getCurrentInstanceControlPort());
//        controlAddresses.add(controlBindAddress);
//        final JeroMQDemultiplexedConnectionRunnable demultiplexedConnectionRunnable = new JeroMQDemultiplexedConnectionRunnable(
//                controlAddresses,
//                getInstanceUuid(),
//                getzContext()
//        );
//        final Thread demultiplexedConnectionThread = new Thread(demultiplexedConnectionRunnable);
//
//        demultiplexedConnectionThread.setDaemon(true);
//        demultiplexedConnectionThread.setName(JeroMQDemultiplexedConnectionService.class.getSimpleName());
//        demultiplexedConnectionThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
//
//        if (atomicDemultiplexedConnectionThread.compareAndSet(null, demultiplexedConnectionThread)) {
//            logger.info("Starting demultiplexed thread and establishing control channel....");
//            demultiplexedConnectionThread.start();
//            demultiplexedConnectionRunnable.blockCurrentThreadUntilControlChannelIsConnected();
//            logger.info("Successfully started demultiplexed thread and established control channel.");
//
//            // now that we have a control channel set up, immediately establish the tcp bind so other instances can talk with this app node
//            bindInvokerAddress();
//        } else {
//            throw new IllegalStateException("Failed to set up demultiplexed connection.");
//        }
//    }
//
//    private void bindInvokerAddress() {
//        final String invokerAddress = RouteRepresentationUtil.buildTcpAddress("*", getCurrentInstanceInvokerPort());
//        logger.info("Issuing bind tcp command to address: {}....", invokerAddress);
////        issueBindTcpCommand(invokerAddress);
//        logger.info("Successfully issued bind tcp command to address: {}....", invokerAddress);
//    }
//
//    void setUpAndStartSrvMonitor() {
//
////        srvMonitorService.registerOnCreatedSrvRecordListener((SrvRecord srvRecord) -> {
////            logger.info("Detected App Node SRV record creation: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////
////            // if we discover the current instance's srv record...
////            if (RouteRepresentationUtil.isHostLocalhost(srvRecord.getHost()) && srvRecord.getPort() == getConnectBindPort()) {
////                logger.info("Skipping issue open backend command to local instance: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////                return; // then ignore it and do not connect
////            }
////
////            final boolean didIssueCommand = connect(srvRecord.getHostAndPort());
////
////            if (didIssueCommand) {
////                logger.info("Successfully issued open backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////            }
////            else {
////                logger.info("Failed to issue open backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////            }
////        });
////
////        srvMonitorService.registerOnUpdatedSrvRecordListener((SrvRecord srvRecord) -> {
////            // for now, ignore updates
////            logger.info("Detected App Node SRV record update: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////        });
////
////        srvMonitorService.registerOnDeletedSrvRecordListener((SrvRecord srvRecord) -> {
////            logger.info("Detected App Node SRV record deletion: host={} port={}",
////                    srvRecord.getHost(), srvRecord.getPort());
////
////            // if we discover the current instance's srv record...
////            if (RouteRepresentationUtil.isHostLocalhost(srvRecord.getHost()) && srvRecord.getPort() == getConnectBindPort()) {
////                logger.info("Skipping issue close backend command to local instance: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////                return; // then ignore it and do not issue unnecessary disconnect command
////            }
////
////            final boolean didIssueCommand = disconnectFromInstance(srvRecord.getHostAndPort());
////
////            if (didIssueCommand) {
////                logger.info("Successfully issued close backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////            }
////            else {
////                logger.info("Failed to issue close backend command for: host={} port={}", srvRecord.getHost(), srvRecord.getPort());
////            }
////        });
////
////        logger.info("Starting SRV record monitor for FQDN: {}...", applicationNodeFqdn);
////        final boolean didStart = srvMonitorService.start(applicationNodeFqdn);
////
////        if (didStart) {
////            logger.info("Successfully started SRV record monitor for FQDN: {}", applicationNodeFqdn);
////        }
////        else {
////            throw new IllegalStateException("Failed to start SRV record monitor for FQDN: " + applicationNodeFqdn);
////        }
//
//    }
//
//    @Override
//    public void stop() {
//
//        final Thread demultiplexedConnectionManagerThread = atomicDemultiplexedConnectionThread.get();
//
//        if (atomicDemultiplexedConnectionThread.compareAndSet(demultiplexedConnectionManagerThread, null)) {
//            demultiplexedConnectionManagerThread.interrupt();
//
//            try {
//                demultiplexedConnectionManagerThread.join();
//            } catch (InterruptedException e) {
//                throw new IllegalStateException("Failed to tear down demultiplexed connection.");
//            }
//        }
//
//    }
//
//    @Override
//    public void issueCommand(final CommandType commandType, final ByteBuffer byteBuffer) {
////        try (final ZContext context = shadow(getzContext());
////             final Connection connection = from(context, c -> c.createSocket(REQ))) {
////            connection.socket().connect(getControlAddress());
////
////            final ZMsg msg = buildControlMsg(commandType, byteBuffer);
////
////            connection.sendMessage(msg);
////            connection.socket().recv();
////        }
//    }
//
//    @Override
//    public Connection connect(final String remoteAddress) {
//        // TODO Implement This
//        return null;
//    }
//
//    //    @Override
////    public boolean connect(final HostAndPort invokerHostAndPort, final HostAndPort controlHostAndPort) {
////        final String invokerTcpAddress = RouteRepresentationUtil.buildTcpAddress(
////                invokerHostAndPort.getHost(),
////                invokerHostAndPort.getPort());
////
////        if (invokerTcpAddress == null) {
////            return false;
////        }
////
////        final String controlTcpAddress = RouteRepresentationUtil.buildTcpAddress(
////                controlHostAndPort.getHost(),
////                controlHostAndPort.getPort()
////        );
////
////        if (controlTcpAddress == null) {
////            return false;
////        }
////
////        issueConnectInstanceCommand(invokerTcpAddress, controlTcpAddress);
////
////        return true;
////    }
////
////    @Override
////    public boolean disconnectFromInstance(final HostAndPort invokerHostAndPort, final HostAndPort controlHostAndPort) {
////        final String invokerTcpAddress = RouteRepresentationUtil.buildTcpAddress(
////                invokerHostAndPort.getHost(),
////                invokerHostAndPort.getPort());
////
////        if (invokerTcpAddress == null) {
////            return false;
////        }
////
////        final String controlTcpAddress = RouteRepresentationUtil.buildTcpAddress(
////                controlHostAndPort.getHost(),
////                controlHostAndPort.getPort()
////        );
////
////        if (controlTcpAddress == null) {
////            return false;
////        }
////
////        issueDisconnectInstanceCommand(invokerTcpAddress, controlTcpAddress);
////
////        return true;
////    }
//
//    public ZContext getzContext() {
//        return zContext;
//    }
//
//    @Inject
//    public void setzContext(ZContext zContext) {
//        this.zContext = zContext;
//    }
//
//    public Integer getCurrentInstanceInvokerPort() {
//        return currentInstanceInvokerPort;
//    }
//
//    @Inject
//    @Named(CURRENT_INSTANCE_INVOKER_PORT_NAME)
//    public void setCurrentInstanceInvokerPort(Integer currentInstanceInvokerPort) {
//        this.currentInstanceInvokerPort = currentInstanceInvokerPort;
//    }
//
//    public Integer getCurrentInstanceControlPort() {
//        return currentInstanceControlPort;
//    }
//
//    @Inject
//    @Named(CURRENT_INSTANCE_CONTROL_PORT_NAME)
//    public void setCurrentInstanceControlPort(Integer currentInstanceControlPort) {
//        this.currentInstanceControlPort = currentInstanceControlPort;
//    }
//
//    public String getControlAddress() {
//        return controlAddress;
//    }
//
//    public String getApplicationNodeFqdn() {
//        return applicationNodeFqdn;
//    }
//
//    @Inject
//    public void setApplicationNodeFqdn(@Named(APPLICATION_NODE_FQDN) String applicationNodeFqdn) {
//        this.applicationNodeFqdn = applicationNodeFqdn;
//    }
//
//    public SrvMonitorService getSrvMonitorService() {
//        return srvMonitorService;
//    }
//
//    @Inject
//    public void setSrvMonitorService(SrvMonitorService srvMonitorService) {
//        this.srvMonitorService = srvMonitorService;
//    }
//
//    public UUID getInstanceUuid() {
//        return instanceUuid;
//    }
//
//    @Inject
//    @Named(CURRENT_INSTANCE_UUID_NAME)
//    public void setInstanceUuid(UUID instanceUuid) {
//        this.instanceUuid = instanceUuid;
//    }
//}
