package com.namazustudios.socialengine.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.remote.RoutingStrategy;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextModule;
import org.zeromq.ZContext;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static com.namazustudios.socialengine.rt.id.ApplicationId.forUniqueName;

/**
 * Combines {@link JeroMQRemoteInvokerModule} with the {@link ClusterContextModule} to make a complete client
 * module with adjustable parameters.
 */
public class JeroMQContextModule extends PrivateModule {

    private Runnable contextBindAction = () -> {};

    private Runnable bindApplicationUuid = () -> {};

    private final JeroMQRemoteInvokerModule jeroMQRemoteInvokerModule = new JeroMQRemoteInvokerModule();

    @Override
    protected void configure() {
        expose(Context.class);
        contextBindAction.run();
        bindApplicationUuid.run();
        install(new ClusterContextModule());
        install(jeroMQRemoteInvokerModule);
    }

    /**
     * Allows for the specification of a specific {@link ZContext} instance
     * @param zContext the {@link ZContext} instance.
     *
     * @return this instance
     */
    public JeroMQContextModule withZContext(final ZContext zContext) {
        contextBindAction = () -> bind(ZContext.class).toInstance(zContext);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withTimeout(int)}}
     *
     * @param timeoutInSeconds the timeout, in seconds
     * @return this instance
     */
    public JeroMQContextModule withTimeout(int timeoutInSeconds) {
        jeroMQRemoteInvokerModule.withTimeout(timeoutInSeconds);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withMinimumConnections(int)}}
     *
     * @param minimumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public JeroMQContextModule withMinimumConnections(int minimumConnections) {
        jeroMQRemoteInvokerModule.withMinimumConnections(minimumConnections);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withMaximumConnections(int)}}
     *
     * @param maximumConnections the minimum number of connections to keep in each connection pool
     * @return this instance
     */
    public JeroMQContextModule withMaximumConnections(int maximumConnections) {
        jeroMQRemoteInvokerModule.withMaximumConnections(maximumConnections);
        return this;
    }

    /**
     * {@see {@link JeroMQRemoteInvokerModule#withDefaultExecutorServiceProvider()}}
     *
     * @return this instance
     */
    public JeroMQContextModule withDefaultExecutorServiceProvider() {
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
    public JeroMQContextModule withExecutorServiceProvider(Provider<ExecutorService> executorServiceProvider) {
        jeroMQRemoteInvokerModule.withExecutorServiceProvider(executorServiceProvider);
        return this;
    }

    /**
     * Given a unique string identifier for the application, this will genrate a unique identifier for the application's
     * unique name using {@link ApplicationId#forUniqueName(String)}.
     *
     * @param applicationUniqueName the unique-string representing the application ID
     * @return this instance
     */
    public JeroMQContextModule withApplicationUniqueName(final String applicationUniqueName) {
        final ApplicationId applicationId = forUniqueName(applicationUniqueName);
        return withApplicationId(applicationId);
    }

    /**
     * Given the supplied {@link ApplicationId}, this will bind it such that it may be used by the various
     * {@link RoutingStrategy} instances.
     *
     * @param applicationId the {@link ApplicationId} for the application
     * @return this instance
     */
    private JeroMQContextModule withApplicationId(final ApplicationId applicationId) {
        bindApplicationUuid = () -> bind(ApplicationId.class)
            .toInstance(applicationId);
        return this;
    }

}
