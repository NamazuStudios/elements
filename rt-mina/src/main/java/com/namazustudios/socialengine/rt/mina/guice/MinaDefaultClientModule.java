package com.namazustudios.socialengine.rt.mina.guice;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.mina.BestEffortIOSessionOutgoingNetworkOperations;
import com.namazustudios.socialengine.rt.mina.MinaClientContainer;
import com.namazustudios.socialengine.rt.mina.ReliableIOSessionOutgoingNetworkOperations;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.guice.MinaScopes;

/**
 * Created by patricktwohig on 9/20/15.
 */
public class MinaDefaultClientModule extends AbstractModule {

    @Override
    protected void configure() {

        final PrivateBinder reliablePrivateBinder = binder().newPrivateBinder();

        reliablePrivateBinder.bindConstant()
                .annotatedWith(Names.named(DefaultClient.MAX_PENDING_REQUESTS))
                .to(maxPendingRequests());

        reliablePrivateBinder.install(new MinaReliableClientModule());
        reliablePrivateBinder.expose(Client.class).annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE));
        reliablePrivateBinder.expose(IoConnector.class).annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE));

        final PrivateBinder bestEffortPrivateBinder = binder().newPrivateBinder();
        bestEffortPrivateBinder.install(new MinaBestEffortClientModule());

        bestEffortPrivateBinder.bindConstant()
                .annotatedWith(Names.named(DefaultClient.MAX_PENDING_REQUESTS))
                .to(maxPendingRequests());

        bestEffortPrivateBinder.bindConstant()
                .annotatedWith(Names.named(BestEffortIOSessionOutgoingNetworkOperations.TIMEOUT))
                .to(bestEffortTimeout());

        bestEffortPrivateBinder.expose(Client.class).annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT));
        bestEffortPrivateBinder.expose(IoConnector.class).annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT));

        binder().bind(ClientContainer.class).to(MinaClientContainer.class);

        binder().bind(ClientEventReceiverMap.class)
                .to(DefaultClientEventReceiverMap.class)
                .in(Scopes.SINGLETON);

    }

    /**
     * Override to adjust the desired timeout for the best effort connector.
     *
     * This defaults to 10 seconds
     *
     * @return the timeout value
     */
    public double bestEffortTimeout() {
        return 10.0;
    }

    /**
     * Override to adjust the desired max pending requests for the {@link Client} instances
     * provided by this module.
     *
     * This defaults to 100 requets
     *
     * @return the timeout value
     */
    public int maxPendingRequests() {
        return 100;
    }

}
