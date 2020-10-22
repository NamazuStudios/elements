package com.namazustudios.socialengine.rt;

import java.util.concurrent.*;

public class SimpleScheduledExecutorContext extends SimpleExecutorContext {

    private final ScheduledExecutorService scheduledExecutorService;

    public SimpleScheduledExecutorContext(final ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public ScheduledFuture<?> schedule(final Runnable runnable, final long l, final TimeUnit timeUnit) {
        return scheduledExecutorService.schedule(wrapRunnable(runnable), l, timeUnit);
    }

    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, long l, TimeUnit timeUnit) {
        return scheduledExecutorService.schedule(wrapCallable(callable), l, timeUnit);
    }

    public ScheduledFuture<?> scheduleAtFixedRatef(final Runnable runnable, final long l, final long l1, final TimeUnit timeUnit) {
        return scheduledExecutorService.scheduleAtFixedRate(wrapRunnable(runnable), l, l1, timeUnit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable runnable, final long l, final long l1, final TimeUnit timeUnit) {
        return scheduledExecutorService.scheduleWithFixedDelay(wrapRunnable(runnable), l, l1, timeUnit);
    }

}
