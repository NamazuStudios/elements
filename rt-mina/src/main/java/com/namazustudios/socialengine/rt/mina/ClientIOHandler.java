package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.*;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class ClientIOHandler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ClientIOHandler.class);

    private IncomingNetworkOperations incomingNetworkOperations;

    private ObjectMapper objectMapper;

    @Inject
    public ClientIOHandler(IncomingNetworkOperations incomingNetworkOperations, ObjectMapper objectMapper) {
        this.incomingNetworkOperations = incomingNetworkOperations;
        this.objectMapper = objectMapper;
    }

    @Override
    public void messageReceived(final IoSession session, final Object message) throws Exception {
        if (message instanceof Response) {
            handle((Response) message);
        } else if (message instanceof Event) {
            handle((Event) message);
        } else {
            LOG.error("Received unexpected message from server: {} ", message);
        }
    }

    private void handle(final Response response) {
        final SimpleResponse simpleResponse = SimpleResponse.builder().from(response).build();
        final Class<?> payloadType = incomingNetworkOperations.getPayloadType(simpleResponse.getResponseHeader());
        final Object payload = objectMapper.convertValue(simpleResponse, payloadType);
        simpleResponse.setPayload(payload);
    }

    private void handle(final Event event) {
        for (final Class<?> eventClass : incomingNetworkOperations.getEventTypes(event.getEventHeader())) {
            final SimpleEvent eventToSend = SimpleEvent.builder().event(event).build();
            final Object payload = objectMapper.convertValue(event.getPayload(), eventClass);
            eventToSend.setPayload(payload);
            incomingNetworkOperations.receive(event, eventClass);
        }
    }

}
