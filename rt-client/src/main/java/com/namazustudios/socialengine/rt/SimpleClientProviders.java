package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Implements various {@link Provider} instances for {@link SimpleClient}
 *
 * Created by patricktwohig on 9/11/15.
 */
public class SimpleClientProviders {

    private SimpleClientProviders() {}

    public static Provider<SimpleClient> getInternalClientProvider() {
        return new Provider<SimpleClient>() {

            @Inject
            @Named(Constants.TRANSPORT_INTERNAL)
            Provider<ClientRequestDispatcher> clientRequestDispatcherProvider;

            @Override
            public SimpleClient get() {
                final ClientRequestDispatcher clientRequestDispatcher = clientRequestDispatcherProvider.get();
                return new SimpleClient(clientRequestDispatcher);
            }

        };
    }

    public static Provider<SimpleClient> getReliableTransportClientProvider() {
        return new Provider<SimpleClient>() {

            @Inject
            @Named(Constants.TRANSPORT_RELIABLE)
            Provider<ClientRequestDispatcher> clientRequestDispatcherProvider;

            @Override
            public SimpleClient get() {
                final ClientRequestDispatcher clientRequestDispatcher = clientRequestDispatcherProvider.get();
                return new SimpleClient(clientRequestDispatcher);
            }

        };
    }

    public static Provider<SimpleClient> getBestEffortTransportClientProvider() {
        return new Provider<SimpleClient>() {

            @Inject
            @Named(Constants.TRANSPORT_BEST_EFFORT)
            Provider<ClientRequestDispatcher> clientRequestDispatcherProvider;

            @Override
            public SimpleClient get() {
                final ClientRequestDispatcher clientRequestDispatcher = clientRequestDispatcherProvider.get();
                return new SimpleClient(clientRequestDispatcher);
            }

        };
    }

}
