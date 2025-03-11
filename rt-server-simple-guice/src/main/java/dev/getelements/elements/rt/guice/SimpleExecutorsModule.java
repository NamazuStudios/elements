package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.provider.CachedThreadPoolProvider;
import dev.getelements.elements.rt.remote.provider.ExecutorServiceFactory;
import dev.getelements.elements.rt.remote.provider.InstanceThreadFactory;
import dev.getelements.elements.rt.remote.provider.ScheduledExecutorServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Constants.SCHEDULER_THREADS;
import static dev.getelements.elements.rt.remote.Instance.*;
import static java.lang.Runtime.getRuntime;

public class SimpleExecutorsModule extends PrivateModule {

    private Runnable bindSchedulerThreads = () -> {};

    /**
     * Determines the default number of scheduler threads as the number of available processors plus one.
     *
     * @return  this instance
     */
    public SimpleExecutorsModule withDefaultSchedulerThreads() {
        return withSchedulerThreads(getRuntime().availableProcessors() + 1);
    }

    /**
     * Specifies the number of scheduler threads.  This number typically can be set low as the actual scheduler threads
     * defer their work to a cached thread pool.  Typically this is set to 1+ the currently availble CPUs
     *
     * @param threads the number of threads
     * @return  this instance
     */
    public SimpleExecutorsModule withSchedulerThreads(int threads) {
        bindSchedulerThreads = () -> bind(Integer.class)
                .annotatedWith(named(SCHEDULER_THREADS))
                .toInstance(threads);
        return this;
    }

    @Override
    protected void configure() {

        bindSchedulerThreads.run();

        bind(ThreadGroup.class)
            .annotatedWith(named(THREAD_GROUP))
            .toInstance(new ThreadGroup(Instance.class.getName()));

        bind(new TypeLiteral<ExecutorServiceFactory<ExecutorService>>(){})
            .annotatedWith(named(EXECUTOR_SERVICE))
            .toProvider(CachedThreadPoolProvider.class);

        bind(new TypeLiteral<ExecutorServiceFactory<ScheduledExecutorService>>(){})
            .annotatedWith(named(SCHEDULED_EXECUTOR_SERVICE))
            .toProvider(ScheduledExecutorServiceProvider.class);

        expose(new TypeLiteral<ExecutorServiceFactory<ExecutorService>>(){})
            .annotatedWith(named(EXECUTOR_SERVICE));

        expose(new TypeLiteral<ExecutorServiceFactory<ScheduledExecutorService>>(){})
            .annotatedWith(named(SCHEDULED_EXECUTOR_SERVICE));

    }

}
