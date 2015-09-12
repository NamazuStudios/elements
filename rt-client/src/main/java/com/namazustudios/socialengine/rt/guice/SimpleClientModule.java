package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.namazustudios.socialengine.rt.Client;
import com.namazustudios.socialengine.rt.SimpleClient;
import com.namazustudios.socialengine.rt.SimpleClientProviders;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class SimpleClientModule extends AbstractModule {

    @Override
    protected final void configure() {
        bindClients();
    }

    protected void bindClients() {

        // Binds the SimpleClient in singletons for each annotated type.  Each
        // is bound in Singleton so there will only be one instance of SimpleCLient
        // for each of the types.

        binder().bind(SimpleClient.class)
                .annotatedWith(Names.named(Client.TRANSPORT_INTERNAL))
                .toProvider(Providers.guicify(SimpleClientProviders.getInternalClientProvider()))
                .in(Scopes.SINGLETON);

        binder().bind(SimpleClient.class)
                .annotatedWith(Names.named(Client.TRANSPORT_BEST_EFFORT))
                .toProvider(Providers.guicify(SimpleClientProviders.getBestEffortTransportClientProvider()))
                .in(Scopes.SINGLETON);

        binder().bind(SimpleClient.class)
                .annotatedWith(Names.named(Client.TRANSPORT_RELIABLE))
                .toProvider(Providers.guicify(SimpleClientProviders.getReliableTransportClientProvider()))
                .in(Scopes.SINGLETON);

        // Binds the Client interface to the simple client

        binder().bind(Client.class)
                .annotatedWith(Names.named(Client.TRANSPORT_INTERNAL))
                .to(SimpleClient.class);

        binder().bind(Client.class)
                .annotatedWith(Names.named(Client.TRANSPORT_BEST_EFFORT))
                .to(SimpleClient.class);

        binder().bind(Client.class)
                .annotatedWith(Names.named(Client.TRANSPORT_RELIABLE))
                .to(SimpleClient.class);

        // Binds the network operations interface to the simple client

        binder().bind(Client.NetworkOperations.class)
                .annotatedWith(Names.named(Client.TRANSPORT_INTERNAL))
                .to(SimpleClient.class);

        binder().bind(Client.NetworkOperations.class)
                .annotatedWith(Names.named(Client.TRANSPORT_BEST_EFFORT))
                .to(SimpleClient.class);

        binder().bind(Client.NetworkOperations.class)
                .annotatedWith(Names.named(Client.TRANSPORT_RELIABLE))
                .to(SimpleClient.class);

    }

}
