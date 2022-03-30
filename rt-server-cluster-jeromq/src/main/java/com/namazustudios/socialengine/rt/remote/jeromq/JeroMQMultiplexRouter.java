package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.*;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.google.common.collect.Multimaps.unmodifiableSortedSetMultimap;
import static com.namazustudios.socialengine.rt.id.NodeId.nodeIdFromBytes;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.OK;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.stripCode;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingCommand.FORWARD;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.exceptionError;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.zeromq.SocketType.DEALER;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQMultiplexRouter {

    private final InstanceId instanceId;

    private final Logger logger;

    private final Stats stats;

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final BiMap<NodeId, Integer> frontends = HashBiMap.create();

    private final BiMap<Integer, NodeId> rFrontends = frontends.inverse();

    private final BiMap<InstanceId, Integer> backends = HashBiMap.create();

    private final BiMap<Integer, InstanceId> rBackends = backends.inverse();

    private final SortedSetMultimap<NodeId, JeroMQInstanceConnectionId> routingTable = TreeMultimap.create();

    public JeroMQMultiplexRouter(final InstanceId instanceId, final ZContext zContext, final ZMQ.Poller poller) {
        this.instanceId = instanceId;
        this.logger = JeroMQRoutingServer.getLogger(getClass(), instanceId);
        this.poller = poller;
        this.zContext = zContext;
        this.stats = new Stats();
    }

    public void poll() {
        rBackends.forEach((index, iid) -> routeToFrontend(index));
        rFrontends.forEach((index, nid) -> routeToBackend(index, nid));
    }
    
    private void routeToFrontend(final int index) {

        if (!poller.pollin(index)) return;

        try {

            final ZMQ.Socket backend = poller.getSocket(index);
            final ZMsg zMsg = ZMsg.recvMsg(backend);
            final ZMsg identity = popIdentity(zMsg);
            final JeroMQControlResponseCode code = stripCode(zMsg);

            switch (code) {
                case OK:
                    respondWithSuccess(zMsg, identity);
                    break;
                default:
                    respondWithFailure(zMsg, identity, code);
                    break;
            }

        } catch (Exception ex) {
            logger.error("Caught exception routing incoming message.", ex);
        }

    }

    private void respondWithSuccess(final ZMsg zMsg, final ZMsg identity) {

        final ZFrame nodeIdHeader = zMsg.removeFirst();
        final NodeId nodeId = nodeIdFromBytes(nodeIdHeader.getData());
        final ZMQ.Socket frontend = getFrontend(nodeId);

        OK.pushResponseCode(zMsg);
        pushIdentity(zMsg, identity);

        if (!zMsg.send(frontend)) {
            logger.error("Failed to send: {}", frontend.errno());
        }

    }

    private void respondWithFailure(final ZMsg zMsg, final ZMsg identity, final JeroMQControlResponseCode code) {
        try {
            final var messageFrame = zMsg.removeFirst();
            final var exceptionCauseFrame = zMsg.removeFirst();
            final var nodeIdHeader = zMsg.removeFirst();

            final var nodeId = nodeIdFromBytes(nodeIdHeader.getData());
            final var frontend = getFrontend(nodeId);

            zMsg.addFirst(nodeIdHeader);
            zMsg.addFirst(exceptionCauseFrame);
            zMsg.addFirst(messageFrame);
            code.pushResponseCode(zMsg);
            pushIdentity(zMsg, identity);

            if (!zMsg.send(frontend)) {
                logger.error("Failed to send: {}", frontend.errno());
            }

        } finally {
            stats.error();
        }
    }

    private ZMQ.Socket getFrontend(final NodeId nodeId) {
        final Integer index = frontends.get(nodeId);
        if (index == null) throw new JeroMQUnroutableNodeException(nodeId);
        return poller.getSocket(index);
    }

    private void routeToBackend(final int index, final NodeId nid) {

        if (!poller.pollin(index)) return;
        final ZMQ.Socket frontend = poller.getSocket(index);

        try {

            // Finds the source and the route to the destination

            final ZMQ.Socket backend = getBackend(nid.getInstanceId());

            // Rebuilds the message and sends it
            final var zMsg = ZMsg.recvMsg(frontend);
            final var identity = popIdentity(zMsg);
            final var nodeIdHeader = new ZFrame(nid.asBytes());

            zMsg.addFirst(nodeIdHeader);
            FORWARD.pushCommand(zMsg);
            pushIdentity(zMsg, identity);

            if (!zMsg.send(backend)) {
                logger.error("Failed to send: {}", backend.errno());
            }

        } catch (JeroMQControlException ex) {

            logger.error("No such instance for node {}", nid, ex);
            final ZMsg response = exceptionError(logger, ex.getCode(), ex);

            if (!response.send(frontend)) {
                logger.error("Failed to send: {}", frontend.errno());
            }

        } catch (Exception ex) {

            logger.error("Caught exception routing outgoing message to {}", nid, ex);
            final ZMsg response = exceptionError(logger, ex);

            if (!response.send(frontend)) {
                logger.error("Failed to send: {}", frontend.errno());
            }

        }

    }

    private ZMQ.Socket getBackend(final InstanceId instanceId) {
        final Integer index = backends.get(instanceId);
        if (index == null) throw new JeroMQUnroutableInstanceException(instanceId);
        return poller.getSocket(index);
    }

    public String openRouteToNode(final NodeId nodeId, final String instanceConnectAddress) {

        final var connectionId = new JeroMQInstanceConnectionId(instanceConnectAddress);

        logger.info("Opening route to node {} via {}", nodeId, connectionId);

        final var frontendIndex = frontends.computeIfAbsent(nodeId, nid -> {
            final var frontend = zContext.createSocket(ROUTER);
            return poller.register(frontend, POLLIN | POLLERR);
        });

        final var frontend = poller.getSocket(frontendIndex);
        final var localBindAddress = getLocalBindAddress(nodeId, connectionId);


        if (routingTable.put(nodeId, connectionId)) {

            final var backendIndex = backends.computeIfAbsent(nodeId.getInstanceId(), iid -> {
                final ZMQ.Socket backend = zContext.createSocket(DEALER);
                stats.addRoute(iid, connectionId);
                return poller.register(backend, POLLIN | POLLERR);
            });

            final var backend = poller.getSocket(backendIndex);
            backend.connect(instanceConnectAddress);
            frontend.bind(localBindAddress);
            stats.addRoute(nodeId);

        }

        return localBindAddress;

    }

    public void closeRoutesViaInstance(final InstanceId instanceId, final String instanceConnectAddress) {

        final var connectionId = new JeroMQInstanceConnectionId(instanceConnectAddress);

        final var nodeIdList = frontends.keySet()
            .stream()
            .filter(nid -> nid.getInstanceId().equals(instanceId))
            .collect(toList());

        if (nodeIdList.isEmpty()) {
            logger.warn("No nodes found for instance {}. Nothing to close.", instanceId);
        } else {

            final var close = nodeIdList
                .stream()
                .map(nodeId -> doCloseFrontend(nodeId, connectionId))
                .reduce(true, (a, b) -> a && b);

            if (close) {
                doCloseBackend(instanceId);
            }

        }

    }

    private boolean doCloseFrontend(final NodeId nodeId, final JeroMQInstanceConnectionId connectionId) {

        frontends.compute(nodeId, (nid,  idx) -> {

            if (idx == null) {
                logger.warn("No route to node {} via {}", nodeId, connectionId);
            } else if (routingTable.remove(nodeId, connectionId)) {

                if (routingTable.containsKey(nodeId)) {
                    final var socket = poller.getSocket(idx);
                    final var localBindAddress = getLocalBindAddress(nodeId, connectionId);
                    socket.unbind(localBindAddress);
                    logger.debug("Other routes exist for {}. Deferring removal. Unbinding {}", nodeId, localBindAddress);
                } else {
                    logger.debug("Removing {}.", nodeId);
                    doClose(idx, format("Node %s", nodeId));
                    stats.removeRoute(nodeId);
                }

            } else {
                logger.warn("No route to node {} via {}", nodeId, connectionId);
            }

            return null;

        });

        return !routingTable.containsKey(nodeId);

    }

    private void doCloseBackend(final InstanceId instanceId) {
        try {
            final var backendIndex = backends.remove(instanceId);
            doClose(backendIndex, format("Instance %s", instanceId));
        } finally {
            stats.removeRoute(instanceId);
        }
    }

    private void doClose(final Integer index, final Object objectId) {

        if (index == null) {
            logger.error("No socket for index {} {}", index, objectId);
        } else {

            final ZMQ.Socket socket = poller.getSocket(index);

            logger.info("Closing socket for sockets[{}] for {}", index, objectId);
            poller.unregister(socket);

            try {
                socket.close();
            } catch (Exception ex) {
                logger.error("Error closing socket for {}", objectId, ex);
            }

        }

    }

    public static String[] getHostAndConnectionId(final String instanceConnectAddress) {
        return instanceConnectAddress.contains("#") ?
            instanceConnectAddress.split("#") :
            new String[] {instanceConnectAddress, ""};
    }

    public String getLocalBindAddress(final NodeId nodeId, final JeroMQInstanceConnectionId connectionId) {
        return format("inproc://%s/mux/%s?%s", instanceId, nodeId.asString(), connectionId);
    }

    public void log() {
        stats.log();
    }

    public SortedSetMultimap<NodeId, JeroMQInstanceConnectionId> getRoutingTable() {
        return unmodifiableSortedSetMultimap(routingTable);
    }

    private class Stats {

        private final SortedMap<NodeId, InstanceId> fRoutes = new TreeMap<>();

        private final SortedMap<InstanceId, JeroMQInstanceConnectionId> bRoutes = new TreeMap<>();

        private final JeroMQDebugCounter errorCounter = new JeroMQDebugCounter();

        private final Runnable log = logger.isDebugEnabled() ? this::doLog : () -> {};

        private void doLog() {

            // Logs all the stats
            final var sb = new StringBuilder();

            sb.append("\nMultiplexer Stats for ").append(instanceId).append(":");
            sb.append("\n  Errors: ").append(errorCounter);
            sb.append("\n  Total Routes: ").append(routingTable.size());
            sb.append("\n  Backend Logical Routes: ").append(bRoutes.size());
            sb.append("\n  Frontend Logical Routes: ").append(fRoutes.size());

            sb.append("\n  Frontend Routes:");

            fRoutes.forEach((nid, iid) ->
                sb.append("\n    ")
                  .append(instanceId.equals(nid.getInstanceId()) ? "L" : "R")
                  .append(nid.isMaster() ? "M: " : "W: ")
                  .append(nid)
                  .append(" -> ")
                  .append(iid)
            );

            sb.append("\n  Backend Routes:");

            bRoutes.forEach((iid, addr) ->
                sb.append("\n    ")
                  .append(instanceId.equals(iid) ? "L: " : "R: ")
                  .append(iid).append(" -> ")
                  .append(addr)
            );

            sb.append("\n  Full Routing Table:");

            routingTable.forEach((nid, addr) -> sb
                .append("\n    ")
                .append(instanceId.equals(nid.getInstanceId()) ? "L" : "R")
                .append(nid.isMaster() ? "M: " : "W: ")
                .append(nid.getInstanceId()).append(" -> ")
                .append(nid).append(" -> ")
                .append(addr)
            );

            logger.debug("{}", sb);

        }

        public void log() {
            log.run();
        }

        public void error() {
            errorCounter.increment();
        }

        public void addRoute(final NodeId nodeId) {
            fRoutes.put(nodeId, nodeId.getInstanceId());
        }

        public void addRoute(final InstanceId instanceId, final JeroMQInstanceConnectionId connectionId) {
            bRoutes.put(instanceId, connectionId);
        }

        public void removeRoute(final NodeId nodeId) {
            fRoutes.remove(nodeId);
        }

        public void removeRoute(final InstanceId instanceId) {
            bRoutes.remove(instanceId);
        }

    }

}

