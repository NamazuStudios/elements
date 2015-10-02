package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.*;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class ClientIOHandler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ClientIOHandler.class);

    private final ObjectMapper objectMapper;

    private final Provider<IncomingNetworkOperations> incomingNetworkOperationsProvider;

    @Inject
    public ClientIOHandler(final ObjectMapper objectMapper,
                           final Provider<IncomingNetworkOperations> incomingNetworkOperationsProvider) {
        this.objectMapper = objectMapper;
        this.incomingNetworkOperationsProvider = incomingNetworkOperationsProvider;
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

        final IncomingNetworkOperations incomingNetworkOperations = incomingNetworkOperationsProvider.get();

        final Class<?> payloadType;

        if (response.getResponseHeader().getCode() == ResponseCode.OK.getCode()) {
            // If the response is okay, then we forward it to the incoming network operations.
            payloadType = incomingNetworkOperations.getPayloadType(response.getResponseHeader());
        } else {
            payloadType = SimpleExceptionResponsePayload.class;
        }

        final Object payload = objectMapper.convertValue(response.getPayload(), payloadType);

        final SimpleResponse simpleResponse = SimpleResponse.builder()
                .from(response)
                .payload(payload)
            .build();

        incomingNetworkOperations.receive(simpleResponse);

    }

    private void handle(final Event event) {

        final IncomingNetworkOperations incomingNetworkOperations = incomingNetworkOperationsProvider.get();

        for (final Class<?> eventClass : incomingNetworkOperations.getEventTypes(event.getEventHeader())) {
            final SimpleEvent eventToSend = SimpleEvent.builder().event(event).build();
            final Object payload = objectMapper.convertValue(event.getPayload(), eventClass);
            eventToSend.setPayload(payload);
            incomingNetworkOperations.receive(event);
        }

    }

}
