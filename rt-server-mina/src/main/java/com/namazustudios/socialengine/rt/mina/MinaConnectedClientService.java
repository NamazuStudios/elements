package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.*;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class MinaConnectedClientService implements ConnectedClientService {

    @Override
    public <PayloadT> Receiver<ResponseHeader, PayloadT> getResponseReceiver(Client client, Class<PayloadT> payloadTClass) {
        return null;
    }

    @Override
    public <PayloadT> Receiver<Event, PayloadT> getEventReceiver(Client client, Class<PayloadT> payloadTClass) {
        return null;
    }

}
