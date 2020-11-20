package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InternalException;

import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Runtime.getRuntime;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SimpleLoadMonitorService implements LoadMonitorService {

    public static final LoadValues UNKNOWN = new LoadValues();

    private static final long REFRESH_RATE = 1;

    private static final TimeUnit REFRESH_UNITS = SECONDS;

    private static final long SHUTDOWN_TIMEOUT = 1;

    private static final TimeUnit SHUTDOWN_UNITS = MINUTES;

    private final OperatingSystemMXBean osBean = getOperatingSystemMXBean();

    private final AtomicReference<LoadMonitorContext> context = new AtomicReference<>();

    @Override
    public void start() {

        final LoadMonitorContext context = new LoadMonitorContext();

        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final LoadMonitorContext context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    @Override
    public double getInstanceQuality() {
        final LoadValues loadValues = getLoadValues();
        return loadValues.getInstanceQuality();
    }

    private LoadMonitorContext getContext() {
        final LoadMonitorContext context = this.context.get();
        if (context == null) throw new IllegalStateException("SimpleLoadMonitorService is not running.");
        return context;
    }

    private LoadValues getLoadValues() {
        final LoadMonitorContext context = getContext();
        final LoadValues loadValues = context.loadValues.get();
        return loadValues == null ? UNKNOWN : loadValues;
    }

    private class LoadMonitorContext {

        private ScheduledExecutorService scheduledExecutorService;

        private final CountDownLatch latch = new CountDownLatch(1);

        private final AtomicReference<LoadValues> loadValues = new AtomicReference<>(UNKNOWN);

        private void start() {

            scheduledExecutorService = newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(this::refresh, 0, REFRESH_RATE, REFRESH_UNITS);

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        private void stop() {

            scheduledExecutorService.shutdown();

            try {
                scheduledExecutorService.awaitTermination(SHUTDOWN_TIMEOUT, SHUTDOWN_UNITS);
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        private void refresh() {
            final double memoryUsage = getMemoryUsage();
            final double loadAverage = osBean.getSystemLoadAverage();
            loadValues.set(new LoadValues(loadAverage, memoryUsage));
            latch.countDown();
        }

        private double getMemoryUsage() {
            long total = getRuntime().totalMemory();
            long inuse = total - getRuntime().freeMemory();
            return (double) (inuse/total);
        }

    }

    private static class LoadValues {

        private final double loadAverage;

        private final double memoryUsage;

        public LoadValues() {
            loadAverage = Float.NaN;
            memoryUsage = Float.NaN;
        }

        public LoadValues(final double loadAverage, final double memoryUsage) {
            this.loadAverage = loadAverage;
            this.memoryUsage = memoryUsage;
        }

        public double getInstanceQuality() {
            return (loadAverage + memoryUsage) / 2;
        }

    }

}
