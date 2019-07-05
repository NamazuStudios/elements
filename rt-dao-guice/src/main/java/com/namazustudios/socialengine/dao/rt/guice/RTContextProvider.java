package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.Injector;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import com.namazustudios.socialengine.rt.jeromq.RouteRepresentationUtil;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQClientModule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.UUID;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.Constants.CURRENT_INSTANCE_UUID_NAME;

public class RTContextProvider implements Provider<Function<String, Context>> {

    private Provider<Injector> injectorProvider;

    private Provider<ConnectionService> connectionServiceProvider;

    private Provider<ApplicationDao> applicationDaoProvider;

    private UUID instanceUuid;

    @Override
    public Function<String, Context> get() {
        return applicationId -> {

            final ApplicationDao applicationDao = getApplicationDaoProvider().get();
            final Application application = applicationDao.getActiveApplication(applicationId);

            final ConnectionService connectionService = getConnectionServiceProvider().get();

            final UUID applicationUuid = RouteRepresentationUtil.buildInprocIdentifierFromString(application.getId());
            final NodeId nodeId = new NodeId(instanceUuid, applicationUuid);
            final String inprocMultiplexAddress = RouteRepresentationUtil.buildMultiplexInprocAddress(nodeId);

            final JeroMQClientModule jeroMQClientModule = new JeroMQClientModule()
                .withDefaultExecutorServiceProvider()
                .withConnectAddress(inprocMultiplexAddress);

            final Injector contextInjector = getInjectorProvider()
                .get()
                .createChildInjector(jeroMQClientModule, jeroMQClientModule);

            final Context context = contextInjector.getInstance(Context.class);

            context.start();

            connectionService.issueConnectInprocCommand("localhost", nodeId);

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

    public Provider<ConnectionService> getConnectionServiceProvider() {
        return connectionServiceProvider;
    }

    @Inject
    public void setConnectionServiceProvider(Provider<ConnectionService> connectionServiceProvider) {
        this.connectionServiceProvider = connectionServiceProvider;
    }

    public Provider<ApplicationDao> getApplicationDaoProvider() {
        return applicationDaoProvider;
    }

    @Inject
    public void setApplicationDaoProvider(Provider<ApplicationDao> applicationDaoProvider) {
        this.applicationDaoProvider = applicationDaoProvider;
    }

    public UUID getInstanceUuid() {
        return instanceUuid;
    }

    @Inject
    @Named(CURRENT_INSTANCE_UUID_NAME)
    public void setInstanceUuid(UUID instanceUuid) {
        this.instanceUuid = instanceUuid;
    }
}
