package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Implements various {@link Provider} instances for {@link DefaultClient}
 *
 * Created by patricktwohig on 9/11/15.
 */
public class SimpleClientProviders {

    private SimpleClientProviders() {}

    public static Provider<DefaultClient> getReliableTransportClientProvider() {
        return new Provider<DefaultClient>() {

            @Inject
            @Named(Constants.TRANSPORT_RELIABLE)
            Provider<OutgoingNetworkOperations> clientRequestDispatcherProvider;

            @Inject
            @Named(DefaultClient.MAX_PENDING_REQUESTS)
            Provider<Integer> maxPendingRequestsProvider;

            @Inject
            Provider<ClientEventReceiverMap> clientEventReceiverMapProvider;

            @Override
            public DefaultClient get() {
                final int maxPendingRequests = maxPendingRequestsProvider.get();
                final OutgoingNetworkOperations outgoingNetworkOperations = clientRequestDispatcherProvider.get();
                final ClientEventReceiverMap clientEventReceiverMap = clientEventReceiverMapProvider.get();
                return new DefaultClient(maxPendingRequests, clientEventReceiverMap, outgoingNetworkOperations);
            }

        };
    }

    public static Provider<DefaultClient> getBestEffortTransportClientProvider() {
        return new Provider<DefaultClient>() {

            @Inject
            @Named(Constants.TRANSPORT_BEST_EFFORT)
            Provider<OutgoingNetworkOperations> clientRequestDispatcherProvider;

            @Inject
            @Named(DefaultClient.MAX_PENDING_REQUESTS)
            Provider<Integer> maxPendingRequestsProvider;

            @Inject
            Provider<ClientEventReceiverMap> clientEventReceiverMapProvider;

            @Override
            public DefaultClient get() {
                final int maxPendingRequests = maxPendingRequestsProvider.get();
                final OutgoingNetworkOperations outgoingNetworkOperations = clientRequestDispatcherProvider.get();
                final ClientEventReceiverMap clientEventReceiverMap = clientEventReceiverMapProvider.get();
                return new DefaultClient(maxPendingRequests, clientEventReceiverMap, outgoingNetworkOperations);
            }

        };
    }

}
