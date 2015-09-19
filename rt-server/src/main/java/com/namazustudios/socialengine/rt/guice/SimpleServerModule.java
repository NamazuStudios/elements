package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
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

        binder().bind(EdgeServer.class)
                .to(SimpleEdgeServer.class)
                .in(Scopes.SINGLETON);

        binder().bind(EdgeRequestDispatcher.class)
                .to(SimpleEdgeRequestDispatcher.class)
                .in(Scopes.SINGLETON);

        binder().bind(new TypeLiteral<ResourceService<EdgeResource>>(){})
                .toProvider(new com.google.inject.Provider<SimpleResourceService<EdgeResource>>() {

                    @Inject
                    Provider<EdgeServer> edgeServerProvider;

                    @Inject
                    Provider<ResourceLockFactory<EdgeResource>> edgeResourceLockFactoryProvider;

                    @Override
                    public SimpleResourceService<EdgeResource> get() {
                        final EdgeServer edgeServer = edgeServerProvider.get();
                        final ResourceLockFactory<EdgeResource> edgeResourceResourceLockFactory = edgeResourceLockFactoryProvider.get();
                        return new SimpleResourceService<>(edgeServer, edgeResourceResourceLockFactory);
                    }

                })
                .in(Scopes.SINGLETON);

        binder().bind(InternalServer.class)
                .to(SimpleInternalServer.class)
                .in(Scopes.SINGLETON);

        binder().bind(new TypeLiteral<ResourceService<InternalResource>>() {})
                .toProvider(new com.google.inject.Provider<SimpleResourceService<InternalResource>>() {

                    @Inject
                    Provider<InternalServer> internalServerProvider;

                    @Inject
                    Provider<ResourceLockFactory<InternalResource>> interResourceLockFactoryProvider;

                    @Override
                    public SimpleResourceService<InternalResource> get() {
                        final InternalServer internalServer = internalServerProvider.get();
                        final ResourceLockFactory<InternalResource> internalResourceResourceLockFactory = interResourceLockFactoryProvider.get();
                        return new SimpleResourceService<>(internalServer, internalResourceResourceLockFactory);
                    }

                }).in(Scopes.SINGLETON);

        binder().bind(ExecutorService.class)
                .annotatedWith(Names.named(AbstractSimpleServer.EXECUTOR_SERVICE))
                .toProvider(Providers.guicify(executorServiceProvider()))
                .in(Scopes.SINGLETON);

        binder().bindConstant()
                .annotatedWith(Names.named(AbstractSimpleServer.MAX_REQUESTS))
                .to(maxRequests());

        binder().bindConstant()
                .annotatedWith(Names.named(AbstractSimpleServer.MAX_EVENTS))
                .to(maxEvents());

        binder().bindConstant()
                .annotatedWith(Names.named(AbstractSimpleServer.MAX_UPDATES_PER_SECOND))
                .to(maxUpdatesPerSecond());

        binder().bind(new TypeLiteral<ResourceLockFactory<EdgeResource>>() {})
                .toProvider(Providers.guicify(edgeResourceLockFactoryProvider()))
                .in(Scopes.SINGLETON);

        binder().bind(new TypeLiteral<ResourceLockFactory<InternalResource>>(){})
                .toProvider(Providers.guicify(interResourceLockFactoryProvider()))
                .in(Scopes.SINGLETON);

        binder().bind(InternalRequestDispatcher.class)
                .to(SimpleInternalRequestDispatcher.class);

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
    protected Provider<ResourceLockFactory<InternalResource>> interResourceLockFactoryProvider() {
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
