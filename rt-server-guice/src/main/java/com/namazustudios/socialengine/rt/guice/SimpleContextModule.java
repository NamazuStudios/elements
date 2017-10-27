package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
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

    @Override
    protected void configure() {

        expose(Context.class);
        expose(IndexContext.class);
        expose(ResourceContext.class);
        expose(SchedulerContext.class);

        // The main context for the application
        bind(Context.class).to(SimpleContext.class).asEagerSingleton();

        // The sub-contexts associated with the main context
        bind(IndexContext.class).to(SimpleIndexContext.class);
        bind(ResourceContext.class).to(SimpleResourceContext.class);
        bind(SchedulerContext.class).to(SimpleSchedulerContext.class);

        bind(ExecutorService.class)
            .annotatedWith(named(SimpleResourceContext.EXECUTOR_SERVICE))
            .toProvider(() -> executorService(SimpleResourceContext.EXECUTOR_SERVICE));

        bind(ExecutorService.class)
            .annotatedWith(named(SimpleIndexContext.EXECUTOR_SERVICE))
            .toProvider(() -> executorService(SimpleIndexContext.EXECUTOR_SERVICE));

        install(new SimpleServicesModule());

    }

    private ExecutorService executorService(final String name) {
        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(SCHEDULED_EXECUTOR_SERVICE);
        return newCachedThreadPool(r -> newThread(r, name, threadCount, logger));
    }

    private Thread newThread(final Runnable runnable, final String name,
                             final AtomicInteger threadCount, final Logger logger) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t , e) -> logger.error("Context exception in {}", t, e));
        thread.setName(format("%s - Thread %d", name, threadCount.incrementAndGet()));
        return thread;
    }

}
