package com.namazustudios.socialengine.rt.guice.example.server;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.edge.EdgeClientSession;
import com.namazustudios.socialengine.rt.edge.EdgeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by patricktwohig on 9/22/15.
 */
public class ServerLogFilter implements EdgeFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerLogFilter.class);

    @Override
    public void filter(Chain chain, EdgeClientSession edgeClientSession, Request request, ResponseReceiver responseReceiver) {
        LOG.debug("Chain: {}.  client: {}. Request: {}.", chain, edgeClientSession, request);
        chain.next(edgeClientSession, request, responseReceiver);
    }

}
