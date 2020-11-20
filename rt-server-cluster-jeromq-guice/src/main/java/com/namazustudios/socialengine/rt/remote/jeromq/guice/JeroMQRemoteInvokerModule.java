package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRemoteInvoker;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.MIN_CONNECTIONS;

public class JeroMQRemoteInvokerModule extends PrivateModule {

    private Runnable bindConnectAddressAction = () -> {};

    private Runnable bindTimeoutAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindMaxConnectionsAction = () -> {};

    private Runnable bindExecutorServiceAction = () -> {};

    /**
     * Specifies the minimum number of connections to keep active, even if the timeout has expired.
     *
     * @param minimumConnections the minimum number of connections to keep issueOpenInprocChannelCommand
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withMinimumConnections(final int minimumConnections) {
        bindMinConnectionsAction = () -> bind(Integer.class)
            .annotatedWith(named(MIN_CONNECTIONS))
            .toInstance(minimumConnections);
        return this;
    }

    /**
     * Specifies the minimum number of connections to allow.
     *
     * @param maximumConnections the minimum number of connections to keep issueOpenInprocChannelCommand
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withMaximumConnections(int maximumConnections) {
        bindMaxConnectionsAction = () -> bind(Integer.class)
                .annotatedWith(named(MAX_CONNECTIONS))
                .toInstance(maximumConnections);
        return this;
    }

    @Override
    protected void configure() {

        bindConnectAddressAction.run();
        bindMinConnectionsAction.run();
        bindMaxConnectionsAction.run();
        bindExecutorServiceAction.run();
        bindTimeoutAction.run();

        bind(RemoteInvoker.class).to(JeroMQRemoteInvoker.class);

        expose(RemoteInvoker.class);

    }

}
