package com.namazustudios.socialengine.rt.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class CachedThreadPoolProvider implements Provider<ExecutorService> {

    private final Class<?> containing;

    public CachedThreadPoolProvider(final Class<?> containing) {
        this.containing = containing;
    }

    @Override
    public ExecutorService get() {

        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(containing);

        return newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t , e) -> logger.error("Context exception in {}", t, e));
            thread.setName(format("%s - Thread %d", containing.getSimpleName(), threadCount.incrementAndGet()));
            return thread;
        });

    }

}
