package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.namazustudios.socialengine.rt.id.NodeId.nodeIdFromBytes;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.*;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static java.util.UUID.randomUUID;
import static org.zeromq.SocketType.DEALER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQDemultiplexRouter {

    private final InstanceId instanceId;

    private final Stats stats;

    private final Logger logger;

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final int frontend;

    private final BiMap<NodeId, Integer> backends = HashBiMap.create();

    private final BiMap<Integer, NodeId> rBackends = backends.inverse();

    public JeroMQDemultiplexRouter(final InstanceId instanceId,
                                   final ZContext zContext,
                                   final ZMQ.Poller poller,
                                   final int frontend) {
        this.instanceId = instanceId;
        this.logger = JeroMQRoutingServer.getLogger(getClass(), instanceId);
        this.zContext = zContext;
        this.poller = poller;
        this.frontend = frontend;
        this.stats = new Stats();
    }

    public void poll() {
        rBackends.forEach((index, iid) -> routeToFrontend(index, iid));
    }

    public String openBinding(final NodeId nodeId) {

        if (backends.containsKey(nodeId)) {
            throw new JeroMQControlException(DUPLICATE_NODE_BINDING);
        }

        final ZMQ.Socket socket = zContext.createSocket(DEALER);
        final int index = poller.register(socket, POLLIN | POLLERR);
        final String localConnectAddress = getLocalBindAddress(nodeId);

        socket.connect(localConnectAddress);
        stats.addRoute(nodeId, localConnectAddress);

        if (backends.put(nodeId, index) != null) {
            logger.error("Attempting to duplicate binding for node {}", nodeId);
            throw new JeroMQControlException(DUPLICATE_NODE_BINDING);
        }

        return localConnectAddress;

    }

    public void closeBindingForNode(final NodeId nodeId) {
        try {
            final Integer index = backends.remove(nodeId);

            if (index == null) {
                logger.warn("No such binding {}", nodeId);
                throw new JeroMQControlException(NO_SUCH_NODE_BINDING);
            } else {
                final ZMQ.Socket socket = poller.getSocket(index);
                poller.unregister(socket);
                socket.close();
                logger.info("Removed binding for node {}", nodeId);
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
        zMsg.send(socket);
    }

    private ZMQ.Socket getSocket(final NodeId nodeId) {
        final Integer backendIndex = backends.get(nodeId);
        if (backendIndex == null) throw new JeroMQUnroutableNodeException(nodeId);
        return poller.getSocket(backendIndex);
    }

    private void routeToFrontend(final int index, final NodeId nid) {

        if (!poller.pollin(index)) return;

        final ZMQ.Socket backend = poller.getSocket(index);
        final ZMQ.Socket frontend = poller.getSocket(this.frontend);

        final ZMsg zMsg = ZMsg.recvMsg(backend);
        final ZMsg identity = popIdentity(zMsg);
        final ZFrame nodeIdFrame = new ZFrame(nid.asBytes());

        zMsg.addFirst(nodeIdFrame);
        OK.pushResponseCode(zMsg);
        pushIdentity(zMsg, identity);
        zMsg.send(frontend);

    }

    public Collection<NodeId> getConnectedNodeIds() {
        return unmodifiableCollection(backends.keySet());
    }

    public String getLocalBindAddress(final NodeId nodeId) {
        return format("inproc://demux/%s/%s?%s", instanceId, nodeId.asString(), randomUUID());
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
