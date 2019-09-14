package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.*;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingCommand.FORWARD;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.exceptionError;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.zeromq.SocketType.DEALER;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQMultiplexRouter {

    private final Logger logger;

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final Map<NodeId, String> localBindAddresses = new HashMap<>();

    private final BiMap<NodeId, Integer> frontends = HashBiMap.create();

    private final BiMap<Integer, NodeId> rFrontends = frontends.inverse();

    private final BiMap<InstanceId, Integer> backends = HashBiMap.create();

    private final BiMap<Integer, InstanceId> rBackends = backends.inverse();

    public JeroMQMultiplexRouter(final InstanceId instanceId, final ZContext zContext, final ZMQ.Poller poller) {
        this.logger = JeroMQRoutingServer.getLogger(getClass(), instanceId);
        this.poller = poller;
        this.zContext = zContext;
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
        final NodeId nodeId = new NodeId(nodeIdHeader.getData());
        final ZMQ.Socket frontend = getFrontend(nodeId);

        OK.pushResponseCode(zMsg);
        pushIdentity(zMsg, identity);
        zMsg.send(frontend);

    }

    private void respondWithFailure(final ZMsg zMsg, final ZMsg identity, final JeroMQControlResponseCode code) {

        final ZFrame messageFrame = zMsg.removeFirst();
        final ZFrame exceptionCauseFrame = zMsg.removeFirst();
        final ZFrame nodeIdHeader = zMsg.removeFirst();

        final NodeId nodeId = new NodeId(nodeIdHeader.getData());
        final ZMQ.Socket frontend = getFrontend(nodeId);

        zMsg.addFirst(nodeIdHeader);
        zMsg.addFirst(exceptionCauseFrame);
        zMsg.addFirst(messageFrame);
        code.pushResponseCode(zMsg);
        pushIdentity(zMsg, identity);
        zMsg.send(frontend);

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
            final ZMsg zMsg = ZMsg.recvMsg(frontend);
            final ZMsg identity = popIdentity(zMsg);
            final ZFrame nodeIdHeader = new ZFrame(nid.asBytes());

            zMsg.addFirst(nodeIdHeader);
            FORWARD.pushCommand(zMsg);
            pushIdentity(zMsg, identity);
            zMsg.send(backend);

        } catch (JeroMQControlException ex) {
            logger.error("No such instance for node {}", nid, ex);
            final ZMsg response = exceptionError(logger, ex.getCode(), ex);
            response.send(frontend);
        } catch (Exception ex) {
            logger.error("Caught exception routing outgoing message to {}", nid, ex);
            final ZMsg response = exceptionError(logger, ex);
            response.send(frontend);
        }

    }

    private ZMQ.Socket getBackend(final InstanceId instanceId) {
        final Integer index = backends.get(instanceId);
        if (index == null) throw new JeroMQUnroutableInstanceException(instanceId);
        return poller.getSocket(index);
    }

    public String openRouteToNode(final NodeId nodeId, final String instanceInvokerAddress) {

        logger.info("Opening route to node {}", nodeId);

        String localBindAddress = localBindAddresses.get(nodeId);
        if (localBindAddress != null) return localBindAddress;

        final ZMQ.Socket backend = zContext.createSocket(DEALER);
        final ZMQ.Socket frontend = zContext.createSocket(ROUTER);

        localBindAddress = getLocalBindAddress(nodeId);
        frontend.bind(localBindAddress);

        backend.connect(instanceInvokerAddress);

        final int backendIndex = poller.register(backend, POLLIN | POLLERR);
        final int frontendIndex = poller.register(frontend, POLLIN | POLLERR);

        frontends.put(nodeId, frontendIndex);
        backends.put(nodeId.getInstanceId(), backendIndex);
        localBindAddresses.put(nodeId, localBindAddress);

        return localBindAddress;

    }

    public void closeRouteToNode(final NodeId nodeId) {

        logger.info("Closing route to node {}", nodeId);

        if (localBindAddresses.remove(nodeId) == null) return;

        final Integer frontendIndex = frontends.remove(nodeId);
        final Integer backendIndex = backends.remove(nodeId.getInstanceId());

        close(backendIndex, nodeId);
        close(frontendIndex, nodeId);

    }

    public void closeRoutesViaInstance(final InstanceId instanceId) {
        frontends.keySet()
            .stream()
            .filter(nid -> nid.getInstanceId().equals(instanceId))
            .collect(toList())
            .forEach(this::closeRouteToNode);
    }

    private void close(final Integer index, final NodeId nodeId) {

        if (index == null) {
            logger.error("No socket for index {} {}", index, nodeId);
            throw new JeroMQControlException(NO_SUCH_NODE_ROUTE);
        } else {
            logger.info("Closing socket for node sockets[{}] for {}", index, nodeId);
        }

        final ZMQ.Socket socket = poller.getSocket(index);
        poller.unregister(socket);

        try {
            socket.close();
        } catch (Exception ex) {
            logger.error("Error closing socket for {}", nodeId, ex);
        }

    }

    public static String getLocalBindAddress(final NodeId nodeId) {
        return format("inproc://mux/%s?%s", nodeId.asString(), randomUUID());
    }

}
