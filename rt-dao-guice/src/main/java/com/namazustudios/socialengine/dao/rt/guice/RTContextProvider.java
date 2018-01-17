package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ClusterClientContextModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQClientModule;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.UUID;
import java.util.function.Function;

public class RTContextProvider implements Provider<Function<String, Context>> {

    private Provider<Injector> injectorProvider;

    private Provider<ConnectionMultiplexer> connectionMultiplexerProvider;

    @Override
    public Function<String, Context> get() {
        return applicationId -> {

            final ConnectionMultiplexer connectionMultiplexer = getConnectionMultiplexerProvider().get();
            final UUID nodeUuid = connectionMultiplexer.getDestinationUUIDForNodeId(applicationId);
            final String connectAddress = connectionMultiplexer.getConnectAddress(nodeUuid);

            final ClusterClientContextModule clusterClientContextModule = new ClusterClientContextModule();
            final JeroMQClientModule jeroMQClientModule = new JeroMQClientModule().withConnectAddress(connectAddress);

            final Injector contextInjector = getInjectorProvider().get().createChildInjector(jeroMQClientModule, clusterClientContextModule);
            final Context context = contextInjector.getInstance(Context.class);
            context.start();

            return context;

        };
    }

    public Provider<Injector> getInjectorProvider() {
        return injectorProvider;
    }

    @Inject
    public void setInjectorProvider(Provider<Injector> injectorProvider) {
        this.injectorProvider = injectorProvider;
    }

    public Provider<ConnectionMultiplexer> getConnectionMultiplexerProvider() {
        return connectionMultiplexerProvider;
    }

    @Inject
    public void setConnectionMultiplexerProvider(Provider<ConnectionMultiplexer> connectionMultiplexerProvider) {
        this.connectionMultiplexerProvider = connectionMultiplexerProvider;
    }

}
