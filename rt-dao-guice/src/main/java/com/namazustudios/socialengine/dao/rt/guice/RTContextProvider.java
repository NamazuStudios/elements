package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.Injector;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.MultiplexedConnectionsManager;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQClientModule;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.UUID;
import java.util.function.Function;

public class RTContextProvider implements Provider<Function<String, Context>> {

    private Provider<Injector> injectorProvider;

    private Provider<MultiplexedConnectionsManager> connectionMultiplexerProvider;

    private Provider<ApplicationDao> applicationDaoProvider;

    @Override
    public Function<String, Context> get() {
        return applicationId -> {

            final ApplicationDao applicationDao = getApplicationDaoProvider().get();
            final Application application = applicationDao.getActiveApplication(applicationId);

            final MultiplexedConnectionsManager multiplexedConnectionsManager = getConnectionMultiplexerProvider().get();
            final UUID nodeUuid = multiplexedConnectionsManager.getDestinationUUIDForNodeId(application.getId());
            final String connectAddress = multiplexedConnectionsManager.getConnectAddress(nodeUuid);

            final JeroMQClientModule jeroMQClientModule = new JeroMQClientModule()
                .withConnectAddress(connectAddress);

            final Injector contextInjector = getInjectorProvider()
                .get()
                .createChildInjector(jeroMQClientModule, jeroMQClientModule);

            final Context context = contextInjector.getInstance(Context.class);

            context.start();
            multiplexedConnectionsManager.open(nodeUuid);

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

    public Provider<MultiplexedConnectionsManager> getConnectionMultiplexerProvider() {
        return connectionMultiplexerProvider;
    }

    @Inject
    public void setConnectionMultiplexerProvider(Provider<MultiplexedConnectionsManager> connectionMultiplexerProvider) {
        this.connectionMultiplexerProvider = connectionMultiplexerProvider;
    }

    public Provider<ApplicationDao> getApplicationDaoProvider() {
        return applicationDaoProvider;
    }

    @Inject
    public void setApplicationDaoProvider(Provider<ApplicationDao> applicationDaoProvider) {
        this.applicationDaoProvider = applicationDaoProvider;
    }

}
