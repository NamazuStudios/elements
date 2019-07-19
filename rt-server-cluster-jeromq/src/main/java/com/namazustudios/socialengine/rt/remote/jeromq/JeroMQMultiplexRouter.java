package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import java.util.*;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.NO_SUCH_ROUTE;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingCommand.FORWARD;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static org.zeromq.SocketType.DEALER;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQMultiplexRouter {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexRouter.class);

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final Map<NodeId, String> localBindAddresses = new HashMap<>();

    private final BiMap<NodeId, Integer> frontends = HashBiMap.create();

    private final BiMap<Integer, NodeId> rFrontends = frontends.inverse();

    private final BiMap<InstanceId, Integer> backends = HashBiMap.create();

    private final BiMap<Integer, InstanceId> rBackends = backends.inverse();

    public JeroMQMultiplexRouter(final ZContext zContext, final ZMQ.Poller poller) {
        this.poller = poller;
        this.zContext = zContext;
    }

    public void poll() {
        rBackends.forEach((index, iid) -> routeToFrontend(index, iid));
        rFrontends.forEach((index, nid) -> routeToBackend(index, nid));
    }

    private void routeToFrontend(final int index, final InstanceId instanceId) {

        if (!poller.pollin(index)) return;

        try {

            final ZMQ.Socket backend = poller.getSocket(index);

            final ZMsg zMsg = ZMsg.recvMsg(backend);
            final ZMsg identity = popIdentity(zMsg);
            final ZFrame nodeIdHeader = zMsg.removeFirst();
            final NodeId nodeId = new NodeId(nodeIdHeader.getData());

            if (!Objects.equals(instanceId, nodeId.getInstanceId())) {
                logger.warn("Inconsistent routing Instance:{} =/= Node:{}", instanceId, nodeId);
            }

            final ZMQ.Socket frontend = getFrontend(nodeId);
            pushIdentity(zMsg, identity);
            zMsg.send(frontend);

        } catch (Exception ex) {
            logger.error("Caught exception routing incoming message.", ex);
        }

    }

    private ZMQ.Socket getFrontend(final NodeId nodeId) {
        final Integer index = frontends.get(nodeId);
        if (index == null) throw new JeroMQControlException(NO_SUCH_ROUTE);
        return poller.getSocket(index);
    }

    private void routeToBackend(final int index, final NodeId nid) {

        if (!poller.pollin(index)) return;

        try {

            // Finds the source and the route to the destination

            final ZMQ.Socket frontend = poller.getSocket(index);
            final ZMQ.Socket backend = getBackend(nid.getInstanceId());

            // Rebuilds the message and sends it
            final ZMsg zMsg = ZMsg.recvMsg(frontend);
            final ZMsg identity = popIdentity(zMsg);
            final ZFrame nodeIdHeader = new ZFrame(nid.asBytes());

            zMsg.addFirst(nodeIdHeader);
            FORWARD.pushCommand(zMsg);
            pushIdentity(zMsg, identity);
            zMsg.send(backend);

        } catch (Exception ex) {
            logger.error("Caught exception routing outgoing message.", ex);
        }

    }

    private ZMQ.Socket getBackend(final InstanceId instanceId) {
        final Integer index = backends.get(instanceId);
        if (index == null) throw new JeroMQControlException(NO_SUCH_ROUTE);
        return poller.getSocket(index);
    }

    public Collection<NodeId> getConnectedPeers() {
        return unmodifiableCollection(frontends.keySet());
    }

    public String openRouteToNode(final NodeId nodeId, final String instanceInvokerAddress) {

        String localBindAddress = localBindAddresses.get(nodeId);
        if (localBindAddress != null) return localBindAddress;

        final ZMQ.Socket backend = zContext.createSocket(DEALER);
        final ZMQ.Socket frontend = zContext.createSocket(ROUTER);

        localBindAddress = getLocalBindAddress(nodeId);
        frontend.bind(localBindAddress);

        backend.connect(instanceInvokerAddress);

        final int backendIndex = poller.register(frontend, POLLIN | POLLERR);
        final int frontendIndex = poller.register(backend, POLLIN | POLLERR);

        frontends.put(nodeId, frontendIndex);
        backends.put(nodeId.getInstanceId(), backendIndex);

        return localBindAddress;

    }

    public static String getLocalBindAddress(final NodeId nodeId) {
        return format("inproc://mux/%s", nodeId.asString());
    }

}
