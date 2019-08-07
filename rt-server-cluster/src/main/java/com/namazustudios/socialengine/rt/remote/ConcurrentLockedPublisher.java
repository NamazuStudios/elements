package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 * Implements a {@link Publisher<T>} using a {@link Lock} to control concurrency.  For each published event, the
 * supplied {@link Lock} will be acquired and then released when all associated {@link Subscription}s have been
 * notified.
 *
 * @param <T> the type of event to publish
 */
public class ConcurrentLockedPublisher<T> implements Publisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentLockedPublisher.class);

    private static Executor dispatch = Executors.newSingleThreadExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(ConcurrentLockedPublisher.class.getName() + " event dispatch.");
        thread.setUncaughtExceptionHandler((t, ex) -> logger.error("Error running InstanceConnectionService", ex));
        return thread;
    });

    private final Lock lock;

    private final List<Consumer<T>> subscribers = new ArrayList<Consumer<T>>();

    /**
     *
     * @param lock
     */
    public ConcurrentLockedPublisher(final Lock lock) {
        this.lock = lock;
    }

    @Override
    public Subscription subscribe(final Consumer<T> consumer) {

        final Subscription subscription = () -> {
            try {
                lock.lock();
                subscribers.removeIf(c -> c == consumer);
            } finally {
                lock.unlock();
            }
        };

        try {
            lock.lock();
            subscribers.add(consumer);
        } finally {
            lock.unlock();
        }

        return subscription;
    }

    @Override
    public void publish(final T t) {
        publish(t, t0 -> {});
    }

    @Override
    public void publishAsync(final T t) {
        publishAsync(t, t0 -> {});
    }

    @Override
    public void publish(final T t, final Consumer<T> onFinish) {
        try {
            lock.lock();
            subscribers.stream().collect(toList()).forEach(c -> c.accept(t));
            onFinish.accept(t);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void publishAsync(final T t, final Consumer<T> onFinish) {
        dispatch.execute(() -> publish(t, onFinish));
    }

}
