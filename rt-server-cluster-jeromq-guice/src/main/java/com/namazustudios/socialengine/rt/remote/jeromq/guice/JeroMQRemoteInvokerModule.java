package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRemoteInvoker;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;

public class JeroMQRemoteInvokerModule extends PrivateModule {

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindMaxConnectionsAction = () -> {};

    /**
     * Specifies the minimum number of connections to keep active, even if the timeout has expired.
     *
     * @param minimumConnections the minimum number of connections to keep issueOpenInprocChannelCommand
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withMinimumConnections(final int minimumConnections) {
        bindMinConnectionsAction = () -> bind(Integer.class)
            .annotatedWith(named(REMOTE_INVOKER_MIN_CONNECTIONS))
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
                .annotatedWith(named(REMOTE_INVOKER_MAX_CONNECTIONS))
                .toInstance(maximumConnections);
        return this;
    }

    @Override
    protected void configure() {
        bindMinConnectionsAction.run();
        bindMaxConnectionsAction.run();
        bind(RemoteInvoker.class).to(JeroMQRemoteInvoker.class);
        expose(RemoteInvoker.class);
    }

}
