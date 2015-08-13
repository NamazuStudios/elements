package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.ConnectedClientService;
import com.namazustudios.socialengine.rt.edge.EdgeResponseReceiver;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class MinaConnectedClientService implements ConnectedClientService {

    @Override
    public EdgeResponseReceiver getResponseReceiver(final Client client, final Request request) {

        final IoSessionClient minaClient;

        try {
             minaClient = (IoSessionClient) client;
        } catch (ClassCastException ex) {
             throw new IllegalArgumentException(ex);
        }

        return new EdgeResponseReceiver() {

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
