package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.NO_SUCH_ROUTE;

public class JeroMQDemultiplexRouter {

    private final ZMQ.Poller poller;

    private final int frontend;

    private final BiMap<NodeId, Integer> backends = HashBiMap.create();

    private final BiMap<Integer, NodeId> rBackends = backends.inverse();

    public JeroMQDemultiplexRouter(final ZMQ.Poller poller, final int frontend) {
        this.poller = poller;
        this.frontend = frontend;
    }

    public void poll() {
        rBackends.forEach((index, iid) -> routeToFrontend(index, iid));
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
        if (backendIndex == null) throw new JeroMQControlException(NO_SUCH_ROUTE);
        return poller.getSocket(backendIndex);
    }

    private void routeToFrontend(final int index, final NodeId nid) {

        final ZMQ.Socket backend = poller.getSocket(index);
        final ZMQ.Socket frontend = poller.getSocket(this.frontend);

        final ZMsg zMsg = ZMsg.recvMsg(backend);
        final ZMsg identity = popIdentity(zMsg);
        final ZFrame nodeIdFrame = new ZFrame(nid.asBytes());

        zMsg.addFirst(nodeIdFrame);
        pushIdentity(zMsg, identity);
        zMsg.send(frontend);

    }

}
