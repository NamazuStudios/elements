package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS;

public class JeroMQInstanceConnectionServiceModule extends PrivateModule {

    private Runnable bindBindAddress = () -> {};

    private Runnable bindRefreshInterval = () -> {};

    @Override
    protected void configure() {

        bindBindAddress.run();
        bindRefreshInterval.run();

        bind(InstanceConnectionService.class)
            .to(JeroMQInstanceConnectionService.class)
            .asEagerSingleton();

        expose(InstanceConnectionService.class);

    }

    /**
     * Specifies the bind address for the underlying {@link JeroMQInstanceConnectionService} where it will accept and
     * receive incoming connections from peers and nother nodes.
     *
     * @param bindAddress the bind address
     * @return this instance
     */
    public JeroMQInstanceConnectionServiceModule withBindAddress(final String bindAddress) {
        bindBindAddress = () -> bind(String.class)
            .annotatedWith(named(JEROMQ_CLUSTER_BIND_ADDRESS))
            .toInstance(bindAddress);
        return this;
    }

    /**
     * Binds the specific refresh interval.
     *
     * @param seconds the refresh interval, in seconds.
     *
     * @return this instance
     */
    public JeroMQInstanceConnectionServiceModule withRefreshIntervalSeconds(long seconds) {
        bindRefreshInterval = () -> bind(long.class)
                .annotatedWith(named(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS))
                .toInstance(seconds);
        return this;
    }

    /**
     * Uses the default refresh interval.
     *
     * @return this instance
     */
    public JeroMQInstanceConnectionServiceModule withDefaultRefreshInterval() {
        return withRefreshIntervalSeconds(30l);
    }

}
