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

public class Publisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);

    private static Executor dispatch = Executors.newSingleThreadExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(Publisher.class.getName() + " event dispatch.");
        thread.setUncaughtExceptionHandler((t, ex) -> logger.error("Error running InstanceConnectionService", ex));
        return thread;
    });

    private final Lock lock;

    private final List<Consumer<T>> subscribers = new ArrayList<Consumer<T>>();

    public Publisher(final Lock lock) {
        this.lock = lock;
    }

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

    public void publish(final T t) {
        publish(t, t0 -> {});
    }

    public void publishAsync(final T t) {
        publishAsync(t, t0 -> {});
    }

    public void publish(final T t, final Consumer<T> onFinish) {
        try {
            lock.lock();
            subscribers.stream().collect(toList()).forEach(c -> c.accept(t));
            onFinish.accept(t);
        } finally {
            lock.unlock();
        }
    }

    public void publishAsync(final T t,  final Consumer<T> onFinish) {
        dispatch.execute(() -> publish(t, onFinish));
    }

}
