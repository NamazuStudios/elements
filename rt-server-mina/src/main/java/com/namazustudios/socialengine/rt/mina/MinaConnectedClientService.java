package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.*;

import java.util.Arrays;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class MinaConnectedClientService implements ConnectedClientService {

    @Override
    public ResponseReceiver getResponseReceiver(Client client) {

        final MinaClient minaClient;

        try {
             minaClient = (MinaClient) client;
        } catch (ClassCastException ex) {
             throw new IllegalArgumentException(ex);
        }

        return new ResponseReceiver() {
            @Override
            public void receive(ResponseCode code, Object payload) {

            }
        };

    }

    @Override
    public EventReceiver getEventReceiver(Client client) {
        return null;
    }



}
