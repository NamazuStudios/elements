package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.provider.CachedThreadPoolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.SimpleScheduler.SCHEDULED_EXECUTOR_SERVICE;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class SimpleContextModule extends PrivateModule {

    private Runnable bindContextAction = () -> bind(Context.class)
            .to(SimpleContext.class)
            .asEagerSingleton();

    /**
     * Specifies the {@link javax.inject.Named} value for the bound {@link Context}.  The context is left unnamed if
     * this is not specified.
     *
     * @param contextName the {@link Context} name
     * @return this instance
     */
    public SimpleContextModule withContextNamed(final String contextName) {
        bindContextAction = () -> bind(Context.class)
            .annotatedWith(named(contextName))
            .to(SimpleContext.class)
            .asEagerSingleton();

        return this;
    }

    @Override
    protected void configure() {

        expose(Context.class);

        // The main context for the application
        bindContextAction.run();

        // The sub-contexts associated with the main context
        install(new SimpleServicesModule());
        install(new SimpleIndexContextModule());
        install(new SimpleResourceContextModule());
        install(new SimpleSchedulerContextModule());

    }

}
