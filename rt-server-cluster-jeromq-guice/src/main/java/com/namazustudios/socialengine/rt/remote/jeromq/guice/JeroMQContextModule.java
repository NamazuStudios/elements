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

    @Override
    protected void configure() {
        expose(Context.class);
        contextBindAction.run();
        bindApplicationUuid.run();
        install(new ClusterContextModule());
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
