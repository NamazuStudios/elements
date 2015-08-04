package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.*;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class MinaConnectedClientService implements ConnectedClientService {

    @Override
    public ResponseReceiver getResponseReceiver(final Client client, final Request request) {

        final IoSessionClient minaClient;

        try {
             minaClient = (IoSessionClient) client;
        } catch (ClassCastException ex) {
             throw new IllegalArgumentException(ex);
        }

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

    @Override
    public EventReceiver getEventReceiver(Client client) {
        return null;
    }



}
