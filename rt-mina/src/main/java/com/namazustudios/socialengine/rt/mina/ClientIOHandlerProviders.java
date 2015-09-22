package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.IncomingNetworkOperations;

import javax.inject.Named;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 9/22/15.
 */
public class ClientIOHandlerProviders {

    private ClientIOHandlerProviders() {}

    public static Provider<ClientIOHandler> getReliableClientIOHandlerProvider() {
        return new Provider<ClientIOHandler>() {

            @Inject
            @Named(Constants.TRANSPORT_RELIABLE)
            private Provider<IncomingNetworkOperations> incomingNetworkOperationsProvider;

            @Inject
            private Provider<ObjectMapper> objectMapperProvider;

            @Override
            public ClientIOHandler get() {
                final IncomingNetworkOperations incomingNetworkOperations = incomingNetworkOperationsProvider.get();
                final ObjectMapper objectMapper = objectMapperProvider.get();
                return new ClientIOHandler(incomingNetworkOperations, objectMapper);
            }

        };
    }

    public static Provider<ClientIOHandler> getBestEffortClientIOHandlerProvider() {
        return new Provider<ClientIOHandler>() {

            @Inject
            @Named(Constants.TRANSPORT_BEST_EFFORT)
            private Provider<IncomingNetworkOperations> incomingNetworkOperationsProvider;

            @Inject
            private Provider<ObjectMapper> objectMapperProvider;

            @Override
            public ClientIOHandler get() {
                final IncomingNetworkOperations incomingNetworkOperations = incomingNetworkOperationsProvider.get();
                final ObjectMapper objectMapper = objectMapperProvider.get();
                return new ClientIOHandler(incomingNetworkOperations, objectMapper);
            }

        };
    }

}
