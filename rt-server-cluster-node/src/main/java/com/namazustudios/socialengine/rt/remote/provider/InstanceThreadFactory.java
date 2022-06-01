package com.namazustudios.socialengine.rt.remote.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

public class InstanceThreadFactory implements ThreadFactory {

    private final Logger logger;

    private final ThreadGroup threadGroup;

    private final AtomicLong threadCount = new AtomicLong();

    @Inject
    public InstanceThreadFactory(final ThreadGroup parent, final String name) {
        logger = LoggerFactory.getLogger(name);
        threadGroup = new ThreadGroup(parent, name);
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final var thread = new Thread(threadGroup, runnable);
        thread.setName(format("%s #%d", logger.getName(), threadCount.incrementAndGet()));
        thread.setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught thread error: {}", t, e));
        return thread;
    }

}
