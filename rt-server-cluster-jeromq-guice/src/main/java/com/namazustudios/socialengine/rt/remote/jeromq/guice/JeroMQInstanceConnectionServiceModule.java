package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.BIND_ADDRESS;

public class JeroMQInstanceConnectionServiceModule extends PrivateModule {

    private Runnable bindBindAddress = () -> {};

    @Override
    protected void configure() {

        bindBindAddress.run();

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
            .annotatedWith(named(BIND_ADDRESS))
            .toInstance(bindAddress);
        return this;
    }

}
