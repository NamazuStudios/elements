package com.namazustudios.socialengine.rt.remote.jeromq;

import org.zeromq.ZMQ;

public class JeroMQDemultiplexRouter {

    private final int invokerIndex;

    private final ZMQ.Poller poller;

    public JeroMQDemultiplexRouter(final ZMQ.Poller poller, final int invokerIndex) {
        this.poller = poller;
        this.invokerIndex = invokerIndex;
    }

    public void poll() {
        if (!poller.pollin(invokerIndex)) return;
        // TODO Handle incoming messages.
    }

}
