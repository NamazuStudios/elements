package com.namazustudios.socialengine.rt.mina.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.mina.BestEffortIOSessionOutgoingNetworkOperations;
import com.namazustudios.socialengine.rt.mina.ReliableIOSessionOutgoingNetworkOperations;
import org.apache.mina.guice.MinaScopes;

/**
 * Created by patricktwohig on 9/20/15.
 */
public class MinaDefaultClientModule extends AbstractModule {

    @Override
    protected void configure() {

        // Binds the DefaultClient in singletons for each annotated type.  Each
        // is bound in Singleton so there will only be one instance of SimpleCLient
        // for each of the types.

        binder().bind(DefaultClient.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT))
                .toProvider(Providers.guicify(SimpleClientProviders.getBestEffortTransportClientProvider()))
                .in(MinaScopes.SESSION);

        binder().bind(DefaultClient.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE))
                .toProvider(Providers.guicify(SimpleClientProviders.getReliableTransportClientProvider()))
                .in(MinaScopes.SESSION);

        // Binds the Client interface to the simple client

        binder().bind(Client.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT))
                .to(DefaultClient.class);

        binder().bind(Client.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE))
                .to(DefaultClient.class);

        // Binds the incoming network operations interface to the simple client

        binder().bind(IncomingNetworkOperations.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT))
                .to(DefaultClient.class);

        binder().bind(IncomingNetworkOperations.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE))
                .to(DefaultClient.class);

        // Binds the outgoing network operations interface to the simple client

        binder().bind(OutgoingNetworkOperations.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT))
                .to(BestEffortIOSessionOutgoingNetworkOperations.class);

        binder().bind(OutgoingNetworkOperations.class)
                .annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE))
                .to(ReliableIOSessionOutgoingNetworkOperations.class);

    }
}
