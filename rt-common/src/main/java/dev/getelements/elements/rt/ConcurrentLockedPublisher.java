package dev.getelements.elements.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Implements a {@link AsyncPublisher <T>} using a {@link Lock} to control concurrency.  For each published event, the
 * supplied {@link Lock} will be acquired and then released when all associated {@link Subscription}s have been
 * notified.
 *
 * @param <T> the type of event to publish
 */
public class ConcurrentLockedPublisher<T> implements AsyncPublisher<T> {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentLockedPublisher.class);

    static Executor dispatch = Executors.newSingleThreadExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(ConcurrentLockedPublisher.class.getName() + " event dispatch.");
        thread.setUncaughtExceptionHandler((t, ex) -> logger.error("Error running InstanceConnectionService", ex));
        return thread;
    });

    private final Lock lock;

    private final Consumer<Runnable> doDispatch;

    private final SimplePublisher<T> publisher = new SimplePublisher<>();

    /**
     * Creates a new {@link ConcurrentLockedPublisher} with the supplied {@link Lock}. This uses an internal thread
     * pool to dispatch events to listeners.
     *
     * @param lock the lock to ensure access to this {@link ConcurrentLockedPublisher<T>}
     */
    public ConcurrentLockedPublisher(final Lock lock) {
        this(lock, dispatch::execute);
    }

    /**
     * Creates a new {@link ConcurrentLockedPublisher} with the supplied {@link Lock}.
     *
     * @param lock the lock to ensure access to this {@link ConcurrentLockedPublisher<T>}
     * @param dispatch a {@link Consumer<Runnable>} which is used to dispatch tasks
     */
    public ConcurrentLockedPublisher(final Lock lock, final Consumer<Runnable> dispatch) {
        this.lock = lock;
        this.doDispatch = dispatch;
    }

    @Override
    public Subscription subscribe(final BiConsumer<Subscription, ? super T> consumer) {
        try {
            lock.lock();
            return publisher.subscribe(consumer);
        } finally {
            lock.unlock();
        }
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
    public void publish(final T t, final Consumer<? super T> onFinish) {
        try {
            lock.lock();
            publisher.publish(t);
            onFinish.accept(t);
        } catch (Exception ex) {
            logger.info("Caught exception dispatching event to {}", onFinish, ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void publishAsync(final T t, final Consumer<T> onFinish) {
        doDispatch.accept(() -> publish(t, onFinish));
    }

    @Override
    public void clear() {
        publisher.clear();
    }

}
