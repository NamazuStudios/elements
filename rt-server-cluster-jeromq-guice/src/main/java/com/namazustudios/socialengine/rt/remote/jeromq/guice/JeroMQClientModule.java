package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.guice.ClusterClientContextModule;
import org.zeromq.ZContext;

import java.util.concurrent.ExecutorService;

/**
 * Combines {@link JeroMQRemoteInvokerModule} with the {@link ClusterClientContextModule} to make a complete client
 * module with adjustable parameters.
 */
public class JeroMQClientModule extends PrivateModule {

    private Runnable contextBindAction = () -> {};

    private final JeroMQRemoteInvokerModule jeroMQRemoteInvokerModule = new JeroMQRemoteInvokerModule();

    @Override
    protected void configure() {
        expose(Context.class);
        contextBindAction.run();
        install(new ClusterClientContextModule());
        install(jeroMQRemoteInvokerModule);
    }

    /**
     * Allows for the specification of a specific {@link ZContext} instance
     * @param zContext the {@link ZContext} instance.
     *
     * @return this instance
     */
    public JeroMQClientModule withZContext(final ZContext zContext) {
        contextBindAction = () -> bind(ZContext.class).toInstance(zContext);
        return this;
    }

    /**
     * {@see {@link JeroMQClientModule#withConnectAddress(String)}}
     *
     * @param connectAddress the connection address
     * @return this instance
     */
    public JeroMQClientModule withConnectAddress(String connectAddress) {
        jeroMQRemoteInvokerModule.withConnectAddress(connectAddress);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withTimeout(int)}}
     *
     * @param timeoutInSeconds the timeout, in seconds
     * @return this instance
     */
    public JeroMQClientModule withTimeout(int timeoutInSeconds) {
        jeroMQRemoteInvokerModule.withTimeout(timeoutInSeconds);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withMinimumConnections(int)}}
     *
     * @param minimumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public JeroMQClientModule withMinimumConnections(int minimumConnections) {
        jeroMQRemoteInvokerModule.withMinimumConnections(minimumConnections);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withMaximumConnections(int)}}
     *
     * @param maximumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public JeroMQClientModule withMaximumConnections(int maximumConnections) {
        jeroMQRemoteInvokerModule.withMaximumConnections(maximumConnections);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withDefaultExecutorServiceProvider()}}
     *
     * @return this instance
     */
    public JeroMQClientModule withDefaultExecutorServiceProvider() {
        jeroMQRemoteInvokerModule.withDefaultExecutorServiceProvider();
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withExecutorServiceProvider(Provider)}}
     *
     * @param executorServiceProvider a {@link Provider<ExecutorService>} instance
     *
     * @return this instance
     */
    public JeroMQClientModule withExecutorServiceProvider(Provider<ExecutorService> executorServiceProvider) {
        jeroMQRemoteInvokerModule.withExecutorServiceProvider(executorServiceProvider);
        return this;
    }

}
