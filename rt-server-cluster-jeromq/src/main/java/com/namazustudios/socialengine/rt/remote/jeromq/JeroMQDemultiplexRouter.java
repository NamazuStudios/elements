package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.namazustudios.socialengine.rt.id.NodeId.nodeIdFromBytes;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.*;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static org.zeromq.SocketType.DEALER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQDemultiplexRouter {

    private final AtomicLong socketUniqueIds = new AtomicLong();

    private final InstanceId instanceId;

    private final Stats stats;

    private final Logger logger;

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final ZMQ.PollItem frontend;

    private final Map<NodeId, ZMQ.PollItem> backends = new LinkedHashMap<>();

    public JeroMQDemultiplexRouter(final InstanceId instanceId,
                                   final ZContext zContext,
                                   final ZMQ.Poller poller,
                                   final ZMQ.PollItem frontend) {
        this.instanceId = instanceId;
        this.logger = JeroMQRoutingServer.getLogger(getClass(), instanceId);
        this.zContext = zContext;
        this.poller = poller;
        this.frontend = frontend;
        this.stats = new Stats();
    }

    public void poll() {
        backends.forEach(this::routeToFrontend);
    }

    public String openBinding(final NodeId nodeId) {

        if (!Objects.equals(instanceId, nodeId.getInstanceId())) {
            logger.error("Attempting to open node binding for remote node {}", nodeId);
            throw new JeroMQControlException(INTERNAL_ERROR);
        }

        logger.info("Opening binding for node {}", nodeId);

        if (backends.containsKey(nodeId)) {
            throw new JeroMQControlException(DUPLICATE_NODE_BINDING);
        }

        final ZMQ.Socket socket = zContext.createSocket(DEALER);
        final int index = poller.register(socket, POLLIN | POLLERR);
        final var item = poller.getItem(index);

        final String localConnectAddress = getLocalBindAddress(nodeId);

        socket.connect(localConnectAddress);
        stats.addRoute(nodeId, localConnectAddress);

        final var existing = backends.putIfAbsent(nodeId, item);

        if (existing != null) {
            // This should not happen. If it does this means something has corrupted the map. Therefore, we will log an
            // error, clean up, and throw an internal exception.
            logger.error("Server has duplicate node binding: {}", nodeId);
            poller.unregister(socket);
            socket.close();
            throw new JeroMQControlException(INTERNAL_ERROR);
        }

        logger.info("Opening binding for node {} with connect address {}", nodeId, localConnectAddress);

        return localConnectAddress;

    }

    public void closeBindingForNode(final NodeId nodeId) {
        try {

            if (!Objects.equals(instanceId, nodeId.getInstanceId())) {
                logger.error("Attempting to close node binding for remote node.");
                throw new JeroMQControlException(INTERNAL_ERROR);
            }

            final var index = backends.remove(nodeId);

            if (index == null) {
                logger.warn("No such binding {}", nodeId);
                throw new JeroMQControlException(NO_SUCH_NODE_BINDING);
            } else {
                logger.info("Removed binding for node {} at socket index {}", nodeId, index);
                final var item = backends.get(nodeId);
                final var socket = item.getSocket();
                poller.unregister(socket);
                socket.close();
                logger.info("Closed binding for node {} at socket index {}", nodeId, index);
            }
        } finally {
            stats.removeRoute(nodeId);
        }
    }

    public void forward(final ZMsg zMsg, final ZMsg identity) {

        final ZFrame nodeIdFrame = zMsg.removeFirst();
        final NodeId nodeId = nodeIdFromBytes(nodeIdFrame.getData());
        final ZMQ.Socket socket = getSocket(nodeId);
        pushIdentity(zMsg, identity);

        if (!zMsg.send(socket)) {
            logger.error("Failed to send: {}", socket.errno());
        }

    }

    private ZMQ.Socket getSocket(final NodeId nodeId) {
        final var item = backends.get(nodeId);
        if (item == null) throw new JeroMQUnroutableNodeException(nodeId);
        return item.getSocket();
    }

    private void routeToFrontend(final NodeId nid, final ZMQ.PollItem item) {

        if (!item.isReadable() && frontend.isWritable()) return;

        final var backend = item.getSocket();
        final var frontend = this.frontend.getSocket();
        final var zMsg = ZMsg.recvMsg(backend);
        final var identity = popIdentity(zMsg);
        final var nodeIdFrame = new ZFrame(nid.asBytes());

        zMsg.addFirst(nodeIdFrame);
        OK.pushResponseCode(zMsg);
        pushIdentity(zMsg, identity);

        if (!zMsg.send(frontend)) {
            logger.error("Failed to send: {}", frontend.errno());
        }

    }

    public Collection<NodeId> getConnectedNodeIds() {
        return unmodifiableCollection(backends.keySet());
    }

    public String getLocalBindAddress(final NodeId nodeId) {
        return format("inproc://demux/%s/%s?uid=%016X",
            instanceId,
            nodeId.asString(),
            socketUniqueIds.incrementAndGet()
        );
    }

    public void log() {
        stats.log();
    }

    private class Stats {

        private final SortedMap<NodeId, String> routes = new TreeMap<>();

        private final JeroMQDebugCounter errorCounter = new JeroMQDebugCounter();

        private final Runnable log = logger.isDebugEnabled() ? this::doLog : () -> {};

        private void doLog() {
            logger.debug("Demultiplexer Stats:");
            logger.debug("  Errors {}", errorCounter);
            logger.debug("  Total Routes: {}", routes.size());
            logger.debug("  Routes:");
            routes.forEach((nid, addr) -> logger.debug("  Node {} (master:{}) -> {} @{})",
                nid, nid.isMaster(),
                addr, backends.get(nid))
            );
        }

        public void log() {
            log.run();
        }

        public void error() {
            errorCounter.increment();
        }

        public void addRoute(final NodeId nodeId, final String nodeBindAddress) {
            routes.put(nodeId, nodeBindAddress);
        }

        public void removeRoute(final NodeId nodeId) {
            routes.remove(nodeId);
        }

    }

}
