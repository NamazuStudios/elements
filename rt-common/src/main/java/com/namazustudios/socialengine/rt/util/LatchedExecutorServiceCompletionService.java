package com.namazustudios.socialengine.rt.util;

import java.util.concurrent.*;

/**
 * Manages tasks queued to an instance of {@link ExecutorService} managing execution through a
 * {@link CompletionServiceLatch}. Unlike the builtin JDK Executor Completion Service this allows for multiple returned
 * {@link Future<?>} types and only tracks the number of tasks. This allows for the tracking of a specific subset of
 * tasks kicked off by an {@link ExecutorService} which may be shared by multiple objects.
 */
public class LatchedExecutorServiceCompletionService {

    private final CompletionServiceLatch latch;

    private final ExecutorService executorService;

    /**
     * Creates a new instance of {@link LatchedExecutorServiceCompletionService} with the executor service and the
     * default {@link CompletionServiceLatch}.
     *
     * @param executorService the {@link ExecutorService} to use
     */
    public LatchedExecutorServiceCompletionService(final ExecutorService executorService) {
        this(executorService, new CompletionServiceLatch());
    }

    /**
     * Creates a new instance of {@link LatchedExecutorServiceCompletionService} with the executor service and a custom
     * {@link CompletionServiceLatch}.
     *
     * @param executorService the {@link ExecutorService} to use
     */
    public LatchedExecutorServiceCompletionService(final ExecutorService executorService,
                                                   final CompletionServiceLatch latch) {
        this.latch = latch;
        this.executorService = executorService;
    }

    /**
     * {@see {@link ExecutorService#submit(Callable)}}
     */
    public <T> Future<T> submit(final Callable<T> callable) {
        return latch.enqueue(callable, c -> executorService.submit(c));
    }

    /**
     * {@see {@link ExecutorService#submit(Runnable, Object)}}
     */
    public <T> Future<T> submit(final Runnable runnable, T t) {
        return latch.enqueue(runnable, r -> executorService.submit(r, t));
    }

    /**
     * {@see {@link ExecutorService#submit(Runnable)}}
     */
    public Future<?> submit(final Runnable runnable) {
        return latch.enqueue(runnable, r -> executorService.submit(r));
    }

    /**
     * {@see {@link ExecutorService#execute(Runnable)}}
     */
    public void execute(final Runnable runnable) {
        latch.enqueue(runnable, r -> {
            executorService.execute(r);
            return null;
        });
    }

    /**
     * Invokes {@link CompletionServiceLatch#stop()} for the {@link CompletionServiceLatch} associated with this
     * {@link LatchedExecutorServiceCompletionService}.
     *
     * {@see {@link CompletionServiceLatch#stop()}}
     */
    public void stop() {
        latch.stop();
    }

}
