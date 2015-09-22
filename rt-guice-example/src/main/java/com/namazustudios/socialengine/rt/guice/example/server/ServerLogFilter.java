package com.namazustudios.socialengine.rt.guice.example.server;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.edge.EdgeClient;
import com.namazustudios.socialengine.rt.edge.EdgeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by patricktwohig on 9/22/15.
 */
public class ServerLogFilter implements EdgeFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerLogFilter.class);

    @Override
    public void filter(Chain chain, EdgeClient edgeClient, Request request, ResponseReceiver responseReceiver) {
        LOG.debug("Chain: {}.  Client: {}. Request: {}.", chain, edgeClient, request);
        chain.next(edgeClient, request, responseReceiver);
    }

}
