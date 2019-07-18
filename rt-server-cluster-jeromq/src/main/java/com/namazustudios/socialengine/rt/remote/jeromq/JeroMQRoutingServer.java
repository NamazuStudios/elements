package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.interrupted;
import static org.zeromq.SocketType.REQ;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

class JeroMQRoutingServer implements AutoCloseable {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final ZMQ.Socket control;

    private final ZMQ.Socket invoker;

    private final int controlIndex;

    private final int invokerIndex;

    private final BiMap<NodeId, Integer> outgoing = HashBiMap.create();

    public JeroMQRoutingServer(final ZContext zContext,
                               final List<String> controlAddresses,
                               final List<String> invokerAddresses) {

        this.zContext = shadow(zContext);

        this.control = zContext.createSocket(REQ);
        this.invoker = zContext.createSocket(ROUTER);

        this.poller = zContext.createPoller(0);
        this.controlIndex = poller.register(this.control, POLLIN | POLLERR);
        this.invokerIndex = poller.register(this.invoker, POLLIN | POLLERR);

        for (final String addr : controlAddresses) this.control.bind(addr);
        for (final String addr : invokerAddresses) this.invoker.bind(addr);

    }

    public void run() {
        while (!interrupted()) {

        }
    }

    @Override
    public void close() {
        poller.close();
        invoker.close();
        control.close();
        zContext.close();
    }

}
