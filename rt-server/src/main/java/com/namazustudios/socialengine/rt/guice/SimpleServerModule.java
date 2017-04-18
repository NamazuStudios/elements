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

        edgeServerPrivateBinder.bind(PathLockFactory.class)
                               .toProvider(Providers.guicify(edgeResourceLockFactoryProvider()));

        edgeServerPrivateBinder.expose(SimpleEdgeContainer.class);
        edgeServerPrivateBinder.expose(new TypeLiteral<Container<EdgeResource>>(){});
        edgeServerPrivateBinder.expose(new TypeLiteral<ResourceService<EdgeResource>>(){});

        final PrivateBinder internalServerBinder = binder().newPrivateBinder();

        internalServerBinder.install(new SimpleInternalServerModule());

        internalServerBinder.bind(PathLockFactory.class)
                            .toProvider(Providers.guicify(internalResourceLockFactoryProvider()));

        internalServerBinder.expose(SimpleInternalContainer.class);
        internalServerBinder.expose(new TypeLiteral<Container<InternalResource>>(){});

        binder().bind(ExecutorService.class)
                .annotatedWith(Names.named(AbstractSimpleContainer.EXECUTOR_SERVICE))
                .toProvider(Providers.guicify(executorServiceProvider()));

    }

    /**
     * Override to include something other than the default settings for {@link ExecutorService}.  The
     * instance is shared.
     *
     * This defaults to a fixed thread pool with a number of threads one greater than the number of availale
     * CPU cores.
     *
     * @return a {@link javax.inject.Provider} for the {@link SimpleEdgeContainer} and the {@link SimpleInternalContainer}
     */
    protected Provider<ExecutorService> executorServiceProvider() {

        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        return () -> Executors.newFixedThreadPool(availableProcessors + 1);

    }

    /**
     * Override to include something other than the default {@link PathLockFactory} instance.
     *
     * This uses the {@link SimplePathLockFactory} to accomplish the task.
     *
     * @return a {@link PathLockFactory} for {@link EdgeResource} instances
     */
    protected Provider<PathLockFactory> edgeResourceLockFactoryProvider() {
        return () -> new SimplePathLockFactory();
    }

    /**
     * Override to include something other than the default {@link PathLockFactory} instance.
     *
     * This uses the {@link SimplePathLockFactory} to accomplish the task.
     *
     * @return a {@link PathLockFactory} for {@link InternalResource} instances
     */
    protected Provider<PathLockFactory> internalResourceLockFactoryProvider() {
        return () -> new SimplePathLockFactory();
    }

}
