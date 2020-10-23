package com.namazustudios.socialengine.rt.util;

import java.util.concurrent.*;

/**
 * Subclass of {@link LatchedExecutorServiceCompletionService} only for use with a {@link ScheduledExecutorService}
 */
public class LatchedScheduledExecutorServiceCompletionService extends LatchedExecutorServiceCompletionService {

    private final CompletionServiceLatch latch;

    private final ScheduledExecutorService scheduledExecutorService;

    public LatchedScheduledExecutorServiceCompletionService(final ScheduledExecutorService scheduledExecutorService,
                                                            final CompletionServiceLatch latch) {
        super(scheduledExecutorService, latch);
        this.latch = latch;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * {@see {@link ScheduledExecutorService#schedule(Runnable, long, TimeUnit)}}
     */
    public ScheduledFuture<?> schedule(final Runnable runnable,
                                       final long l,
                                       final TimeUnit timeUnit) {
        return latch.enqueue(runnable, r -> scheduledExecutorService.schedule(r, l, timeUnit));
    }

    /**
     * {@see {@link ScheduledExecutorService#schedule(Callable, long, TimeUnit)}}
     */
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable,
                                           long l,
                                           final TimeUnit timeUnit) {
        return latch.enqueue(callable, c -> scheduledExecutorService.schedule(c, l, timeUnit));
    }

    /**
     * {@see {@link ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)}}
     */
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable runnable,
                                                   final long l,
                                                   final long li,
                                                   final TimeUnit timeUnit) {
        return latch.enqueue(runnable, r -> scheduledExecutorService.scheduleAtFixedRate(r, l,li, timeUnit));
    }

    /**
     * {@see {@link ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}}
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable runnable,
                                                     final long l,
                                                     final long li,
                                                     final TimeUnit timeUnit) {
        return latch.enqueue(runnable, r -> scheduledExecutorService.scheduleWithFixedDelay(r, l, li, timeUnit));
    }

}
