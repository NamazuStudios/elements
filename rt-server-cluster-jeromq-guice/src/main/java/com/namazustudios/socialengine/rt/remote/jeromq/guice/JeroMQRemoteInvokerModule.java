package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.jeromq.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.SimpleAsyncConnectionPool;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRemoteInvoker;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.jeromq.AsyncConnectionPool.*;

public class JeroMQRemoteInvokerModule extends PrivateModule {

    private Runnable bindConnectAddressAction = () -> {};

    private Runnable bindTimeoutAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindMaxConnectionsAction = () -> {};

    private Runnable bindExecutorServiceAction = () -> {};

    /**
     * Specifies the connection timeout.  If a connection isn't used for the specified period of time, the underlying
     * connection is terminated and removed.  Leaving this unspecified will not assign any properties and leave it to
     * external means to configure the underlying module.
     *
     * @param timeoutInSeconds the timeout value, in seconds.
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withTimeout(final int timeoutInSeconds) {
        bindTimeoutAction = () -> bind(Integer.class)
            .annotatedWith(named(TIMEOUT))
            .toInstance(timeoutInSeconds);
        return this;
    }

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
        bind(AsyncConnectionPool.class).to(SimpleAsyncConnectionPool.class);

        expose(RemoteInvoker.class);

    }

}
