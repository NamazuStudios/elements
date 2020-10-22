package com.namazustudios.socialengine.rt.remote.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class CachedThreadPoolProvider implements Provider<ExecutorService> {

    private final String name;

    private final Class<?> containing;

    public CachedThreadPoolProvider(final Class<?> containing) {
        this.containing = containing;
        name = containing.getSimpleName();
    }

    public CachedThreadPoolProvider(final Class<?> containing, final String qualifier) {
        this.containing = containing;
        this.name = format("%s.%s", containing.getSimpleName(), qualifier);
    }

    @Override
    public ExecutorService get() {

        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(containing);

        return newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t , e) -> logger.error("Fatal Error: {}", t, e));
            thread.setName(format("%s %s - #%d", containing.getSimpleName(), name, threadCount.incrementAndGet()));
            return thread;
        });

    }

}
