package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeClient;
import com.namazustudios.socialengine.rt.edge.ConnectedEdgeClientService;
import com.namazustudios.socialengine.rt.ResponseReceiver;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class MinaConnectedEdgeClientService implements ConnectedEdgeClientService {

    @Override
    public ResponseReceiver getResponseReceiver(final EdgeClient edgeClient, final Request request) {

        final IoSessionClient minaClient;

        try {
             minaClient = (IoSessionClient) edgeClient;
        } catch (ClassCastException ex) {
             throw new IllegalArgumentException(ex);
        }

        return getResponseReceiver(minaClient, request);

    }

    public ResponseReceiver getResponseReceiver(final IoSessionClient minaClient, final Request request) {

        return new ResponseReceiver() {

            @Override
            public void receive(int code, Object payload) {

                final SimpleResponseHeader simpleResponseHeader = new SimpleResponseHeader();
                simpleResponseHeader.setCode(code);
                simpleResponseHeader.setSequence(request.getHeader().getSequence());

                final SimpleResponse simpleResponse = new SimpleResponse();
                simpleResponse.setResponseHeader(simpleResponseHeader);
                simpleResponse.setPayload(payload);

                minaClient.getIoSession().write(simpleResponse);

            }
        };

    }

}
