package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeClientSession;
import com.namazustudios.socialengine.rt.edge.ConnectedEdgeClientService;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class MinaConnectedEdgeClientService implements ConnectedEdgeClientService {

    private static final Logger LOG = LoggerFactory.getLogger(MinaConnectedEdgeClientService.class);

    @Override
    public ResponseReceiver getResponseReceiver(final EdgeClientSession edgeClientSession, final Request request) {

        final IoSessionClientSession minaClient;

        try {
             minaClient = (IoSessionClientSession) edgeClientSession;
        } catch (ClassCastException ex) {
             throw new IllegalArgumentException(ex);
        }

        return getResponseReceiver(minaClient, request);

    }

    public ResponseReceiver getResponseReceiver(final IoSessionClientSession minaClient, final Request request) {

        return new ResponseReceiver() {

            @Override
            public void receive(Response response) {

                final SimpleResponse simpleResponse = SimpleResponse.builder()
                        .from(response)
                    .build();

                if (request.getHeader().getSequence() != simpleResponse.getResponseHeader().getSequence()) {
                    LOG.warn("Out of sequence response {} {}", request, response);
                }

                minaClient.getIoSession().write(simpleResponse);

            }
        };

    }

}
