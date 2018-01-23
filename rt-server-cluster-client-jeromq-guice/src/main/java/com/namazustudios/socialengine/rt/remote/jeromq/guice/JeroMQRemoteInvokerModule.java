package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.CONNECT_ADDRESS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.TIMEOUT;

public class JeroMQRemoteInvokerModule extends PrivateModule {

    private Runnable bindConnectAddressAction = () -> {};

    private Runnable bindTimeoutAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    /**
     * Specifies the connect address used by the underlying {@link JeroMQRemoteInvoker}.  This provides a binding for
     * the option {@link JeroMQRemoteInvoker#CONNECT_ADDRESS}.  Leaving this unspecified will not assign any properties
     * and leave it to external means to configure the underlying module.
     *
     * @param connectAddress the connect address
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withConnectAddress(final String connectAddress) {
        bindConnectAddressAction = () -> bind(String.class)
            .annotatedWith(named(CONNECT_ADDRESS))
            .toInstance(connectAddress);
        return this;
    }

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
     * @param minimumConnections the minimum number of connections to keep open
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withMinimumConnections(final int minimumConnections) {
        bindMinConnectionsAction = () -> bind(Integer.class)
            .annotatedWith(named(MIN_CONNECTIONS))
            .toInstance(minimumConnections);
        return this;
    }

    @Override
    protected void configure() {

        install(new FSTPayloadReaderWriterModule());

        bindConnectAddressAction.run();
        bindMinConnectionsAction.run();
        bindTimeoutAction.run();

        bind(RemoteInvoker.class).to(JeroMQRemoteInvoker.class).asEagerSingleton();
        bind(ConnectionPool.class).to(DynamicConnectionPool.class);

        expose(RemoteInvoker.class);

    }

}
