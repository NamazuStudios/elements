package com.namazustudios.socialengine.rt.guice;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.*;
import com.namazustudios.socialengine.rt.internal.*;

import javax.inject.Provider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by patricktwohig on 9/2/15.
 */
public class SimpleServerModule extends AbstractModule {

    @Override
    protected void configure() {

        final PrivateBinder edgeServerPrivateBinder = binder().newPrivateBinder();

        edgeServerPrivateBinder.install(new SimpleEdgeServerModule());

        edgeServerPrivateBinder.bind(new TypeLiteral<ResourceLockFactory<EdgeResource>>() {})
                               .toProvider(Providers.guicify(edgeResourceLockFactoryProvider()));

        edgeServerPrivateBinder.expose(EdgeServer.class);
        edgeServerPrivateBinder.expose(SimpleEdgeServer.class);
        edgeServerPrivateBinder.expose(new TypeLiteral<ResourceService<EdgeResource>>(){});

        final PrivateBinder internalServerBinder = binder().newPrivateBinder();

        internalServerBinder.install(new SimpleInternalServerModule());

        internalServerBinder.bind(new TypeLiteral<ResourceLockFactory<InternalResource>>() {})
                            .toProvider(Providers.guicify(internalResourceLockFactoryProvider()));

        internalServerBinder.expose(InternalServer.class);
        internalServerBinder.expose(SimpleInternalServer.class);

        binder().bind(ExecutorService.class)
                .annotatedWith(Names.named(AbstractSimpleServer.EXECUTOR_SERVICE))
                .toProvider(Providers.guicify(executorServiceProvider()));

        binder().bindConstant()
                .annotatedWith(Names.named(AbstractSimpleServer.MAX_REQUESTS))
                .to(maxRequests());

        binder().bindConstant()
                .annotatedWith(Names.named(AbstractSimpleServer.MAX_EVENTS))
                .to(maxEvents());

        binder().bindConstant()
                .annotatedWith(Names.named(AbstractSimpleServer.MAX_UPDATES_PER_SECOND))
                .to(maxUpdatesPerSecond());

    }

    /**
     * Override to include something other than the default settings for {@link ExecutorService}.  The
     * instance is shared.
     *
     * This defaults to a fixed thread pool with a number of threads one greater than the number of availale
     * CPU cores.
     *
     * @return a {@link javax.inject.Provider} for the {@link SimpleEdgeServer} and the {@link SimpleInternalServer}
     */
    protected Provider<ExecutorService> executorServiceProvider() {

        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        return new Provider<ExecutorService>() {
            @Override
            public ExecutorService get() {
                return Executors.newFixedThreadPool(availableProcessors + 1);
            }
        };

    }

    /**
     * Override to include something other than the default {@link ResourceLockFactory} instance.
     *
     * This uses the {@link ProxyLockFactory} to accomplish the task.
     *
     * @return a {@link ResourceLockFactory} for {@link EdgeResource} instances
     */
    protected Provider<ResourceLockFactory<EdgeResource>> edgeResourceLockFactoryProvider() {
        return new Provider<ResourceLockFactory<EdgeResource>>() {
            @Override
            public ResourceLockFactory<EdgeResource> get() {
                return ProxyLockFactory.edgeResourceProxyLockFactory();
            }
        };
    }

    /**
     * Override to include something other than the default {@link ResourceLockFactory} instance.
     *
     * This uses the {@link ProxyLockFactory} to accomplish the task.
     *
     * @return a {@link ResourceLockFactory} for {@link InternalResource} instances
     */
    protected Provider<ResourceLockFactory<InternalResource>> internalResourceLockFactoryProvider() {
        return new Provider<ResourceLockFactory<InternalResource>>() {
            @Override
            public ResourceLockFactory<InternalResource> get() {
                return ProxyLockFactory.internalResourceProxyLockFactory();
            }
        };
    }

    /**
     * Override to change the maximum number of requests that the server will accept per loop.
     *
     * @see {@link AbstractSimpleServer#MAX_REQUESTS}
     *
     * @return the maximum number of requets.
     */
    private int maxRequests() {
        return 1000;
    }

    /**
     * Override to change the maximum number of requests that the server will accept per loop.
     *
     * @see {@link AbstractSimpleServer#MAX_REQUESTS}
     *
     * @return the maximum number of requets.
     */
    private int maxEvents() {
        return 1000;
    }

    /**
     * Override to change the maximum number of updates per second.
     *
     * @return the max updates per second
     */
    private int maxUpdatesPerSecond() {
        return 120;
    }

}
