package dev.getelements.elements.rt.remote.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Provider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;

public class CPUCountThreadPoolProvider implements Provider<ExecutorService> {

    private final String name;

    private final Class<?> containing;

    private static final int MIN_MULTIPLIER = 2;

    private static final int MAX_MULTIPLIER = 100;

    private static final int MIN_POOL_SIZE = getRuntime().availableProcessors() * MIN_MULTIPLIER;

    private static final int MAX_POOL_SIZE = getRuntime().availableProcessors() * MAX_MULTIPLIER;

    private static final long TIMEOUT_TIME = 1;

    private static final TimeUnit TIMEOUT_UNIT = MINUTES;

    public CPUCountThreadPoolProvider(final Class<?> containing) {
        this.containing = containing;
        name = containing.getSimpleName();
    }

    public CPUCountThreadPoolProvider(final Class<?> containing, final String qualifier) {
        this.containing = containing;
        this.name = format("%s.%s", containing.getSimpleName(), qualifier);
    }

    @Override
    public ExecutorService get() {

        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(containing);

        return new ThreadPoolExecutor(MIN_POOL_SIZE, MAX_POOL_SIZE, TIMEOUT_TIME, TIMEOUT_UNIT, new SynchronousQueue<>(),
        r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t , e) -> logger.error("Fatal Error: {}", t, e));
            thread.setName(format("%s - #%d", name, threadCount.incrementAndGet()));
            return thread;
        });

    }

}
