package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Collection;

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

    private static final Logger logger = LoggerFactory.getLogger(JeroMQDemultiplexRouter.class);

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final int frontend;

    private final BiMap<NodeId, Integer> backends = HashBiMap.create();

    private final BiMap<Integer, NodeId> rBackends = backends.inverse();

    public JeroMQDemultiplexRouter(final ZContext zContext, final ZMQ.Poller poller, final int frontend) {
        this.zContext = zContext;
        this.poller = poller;
        this.frontend = frontend;
    }

    public void poll() {
        rBackends.forEach((index, iid) -> routeToFrontend(index, iid));
    }

    public String openBinding(final NodeId nodeId) {

        if (backends.containsKey(nodeId)) throw new JeroMQControlException(BINDING_ALREADY_EXISTS);

        final ZMQ.Socket socket = zContext.createSocket(DEALER);
        final int index = poller.register(socket, POLLIN | POLLERR);
        final String localConnectAddress = getLocalConnectAddress(nodeId);
        socket.connect(localConnectAddress);
        backends.put(nodeId, index);

        return localConnectAddress;
    }

    public void closeBindingForNode(final NodeId nodeId) {

        final Integer index = backends.remove(nodeId);

        if (index == null) {
            logger.warn("No such binding {}", nodeId);
        } else {
            final ZMQ.Socket socket = poller.getSocket(index);
            socket.close();
            logger.info("Removed binding for node {}", nodeId);
        }

    }

    public void forward(final ZMsg zMsg, final ZMsg identity) {
        final ZFrame nodeIdFrame = zMsg.removeFirst();
        final NodeId nodeId = new NodeId(nodeIdFrame.getData());
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

    public static String getLocalConnectAddress(final NodeId nodeId) {
        return format("inproc://demux/%s?%s", nodeId.asString(), randomUUID());
    }

}
