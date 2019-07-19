package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;

public class JeroMQDemultiplexRouter {

    private final int invokerIndex;

    private final ZMQ.Poller poller;

    private final Map<NodeId, String> localBindAddresses = new HashMap<>();

    private final BiMap<NodeId, Integer> frontends = HashBiMap.create();

    private final BiMap<Integer, NodeId> rFrontends = frontends.inverse();

    private final BiMap<InstanceId, Integer> backends = HashBiMap.create();

    private final BiMap<Integer, InstanceId> rBackends = backends.inverse();

    public JeroMQDemultiplexRouter(final ZMQ.Poller poller, final int invokerIndex) {
        this.poller = poller;
        this.invokerIndex = invokerIndex;
    }

    public void poll() {
        if (!poller.pollin(invokerIndex)) return;
        // TODO Handle incoming messages.
    }

    public boolean handle(ZMQ.Socket socket, ZMsg msg, JeroMQControlCommand command, ZMsg zMsg) {
        return false;
    }

}
