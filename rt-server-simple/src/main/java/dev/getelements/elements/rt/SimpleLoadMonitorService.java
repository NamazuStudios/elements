package dev.getelements.elements.rt;

import dev.getelements.elements.rt.exception.InternalException;

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

    private static double QUALITY_SCALE = 100;

    private static final long REFRESH_RATE = 1;

    private static final TimeUnit REFRESH_UNITS = SECONDS;

    private static final long SHUTDOWN_TIMEOUT = 1;

    private static final TimeUnit SHUTDOWN_UNITS = MINUTES;

    private static final double LOAD_AVERAGE_WEIGHT = 1.0;

    private static final double MEMORY_USAGE_WEIGHT = 1.0;

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
            final double total = getRuntime().totalMemory();
            final double inuse = total - getRuntime().freeMemory();
            return inuse/total;
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
            return QUALITY_SCALE * getWeightedAverage();
        }

        private double getWeightedAverage() {

            // If the load average isn't available, then we must rely entirely on memory usage instead.
            if (loadAverage < 0) return memoryUsage;

            // Otherwise, we average the two values.
            final var load = loadQuality() * LOAD_AVERAGE_WEIGHT;
            final var memory = memoryQuality() * MEMORY_USAGE_WEIGHT;

            if (Double.isNaN(load)) return memoryUsage;
            if (Double.isNaN(memory)) return loadAverage;

            return (load + memory) / 2;

        }

        private double loadQuality() {
            final double systemCpuCores = getRuntime().availableProcessors();
            return Double.isNaN(loadAverage) ? Double.NaN : 1.0 - (loadAverage / systemCpuCores);
        }

        private double memoryQuality() {
            return Double.isNaN(memoryUsage) ? Double.NaN : 1.0 - memoryUsage;
        }

    }

}
