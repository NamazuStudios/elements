package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InternalException;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleExecutorContext {

    private final ExecutorService executorService;

    private final AtomicBoolean accepting = new AtomicBoolean(false);

    private final Semaphore semaphore;

    public SimpleExecutorContext(final ExecutorService executorService, final Semaphore semaphore) {
        this.semaphore = semaphore;
        this.executorService = executorService;
    }

    public <T> Future<T> submit(final Callable<T> callable) {
        return executorService.submit(wrapCallable(callable));
    }

    public <T> Future<T> submit(final Runnable runnable, T t) {
        return executorService.submit(wrapRunnable(runnable), t);
    }

    public Future<?> submit(final Runnable runnable) {
        return executorService.submit(wrapRunnable(runnable));
    }

    public void execute(final Runnable runnable) {
        executorService.submit(wrapRunnable(runnable));
    }

    private void acquire() {
        try {

            semaphore.acquire(2);

            if (!accepting.get()) {
                semaphore.release();
                throw new IllegalStateException("Not accepting new work.");
            }

        } catch (InterruptedException ex) {
            throw new InternalException(ex);
        } finally {
            semaphore.release();
        }
    }

    protected void release() {
        semaphore.release();
    }

    public void stop() {

        try {
            semaphore.acquire(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new InternalException(e);
        }

        accepting.set(false);
        semaphore.release(Integer.MAX_VALUE);

    }

    protected <T> Callable<T> wrapCallable(final Callable<T> callable) {

        acquire();

        return () -> {
            try {
                return callable.call();
            } finally {
                semaphore.release();
            }
        };

    }

    protected Runnable wrapRunnable(final Runnable runnable) {

        acquire();

        return () -> {
            try {
                runnable.run();
            } finally {
                semaphore.release();
            }
        };

    }

}
