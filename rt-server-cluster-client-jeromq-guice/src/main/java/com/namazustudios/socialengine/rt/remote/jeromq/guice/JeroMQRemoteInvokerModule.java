package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.SimpleConnectionPool;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;

import java.util.concurrent.ExecutorService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.CONNECT_ADDRESS;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.ASYNC_EXECUTOR_SERVICE;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class JeroMQRemoteInvokerModule extends PrivateModule {

    private Runnable bindConnectAddressAction = () -> {};

    private Runnable bindTimeoutAction = () -> {};

    private Runnable bindMinConnectionsAction = () -> {};

    private Runnable bindMaxConnectionsAction = () -> {};

    private Runnable bindExecutorServiceAction = () -> {};

    /**
     * Binds the default {@link ExecutorService} which is used to handle background tasks in the {@link RemoteInvoker}.
     *
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withDefaultExecutorServiceProvider() {
        return withExecutorServiceProvider(() -> newCachedThreadPool());
    }

    /**
     * Specifies the {@link Provider<ExecutorService>} used by the {@link RemoteInvoker} instance.
     *
     * @param executorServiceProvider the {@link Provider<ExecutorService>}
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withExecutorServiceProvider(final Provider<ExecutorService> executorServiceProvider) {
        bindExecutorServiceAction = () -> bind(ExecutorService.class)
            .annotatedWith(named(ASYNC_EXECUTOR_SERVICE))
            .toProvider(executorServiceProvider);
        return this;
    }

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
            .annotatedWith(named(ConnectionPool.TIMEOUT))
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
            .annotatedWith(named(ConnectionPool.MIN_CONNECTIONS))
            .toInstance(minimumConnections);
        return this;
    }

    /**
     * Specifies the minimum number of connections to allow.
     *
     * @param maximumConnections the minimum number of connections to keep open
     * @return this instance
     */
    public JeroMQRemoteInvokerModule withMaximumConnections(int maximumConnections) {
        bindMaxConnectionsAction = () -> bind(Integer.class)
                .annotatedWith(named(ConnectionPool.MAX_CONNECTIONS))
                .toInstance(maximumConnections);
        return this;
    }

    @Override
    protected void configure() {

        install(new FSTPayloadReaderWriterModule());

        bindConnectAddressAction.run();
        bindMinConnectionsAction.run();
        bindMaxConnectionsAction.run();
        bindExecutorServiceAction.run();
        bindTimeoutAction.run();

        bind(RemoteInvoker.class).to(JeroMQRemoteInvoker.class).asEagerSingleton();
        bind(ConnectionPool.class).to(SimpleConnectionPool.class);

        expose(RemoteInvoker.class);

    }

}
