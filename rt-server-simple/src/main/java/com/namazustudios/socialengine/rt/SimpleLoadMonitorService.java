package com.namazustudios.socialengine.rt;

import com.google.common.util.concurrent.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleLoadMonitorService implements LoadMonitorService {
    public static final double UNKNOWN_LOAD_AVERAGE = -1.0;

    // TODO: maybe make a Loggable interface with a protected static final Logger, would need to test the lookup being defined in an interface
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AtomicDouble atomicLoadAverage = new AtomicDouble(UNKNOWN_LOAD_AVERAGE);

    private final AtomicReference<Thread> atomicLoadMonitorThread = new AtomicReference<>();

    @Override
    public void start() {
        final Thread loadMonitorThread = new Thread(() -> {
            final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            while (!Thread.interrupted()) {
                double loadAverage = osBean.getSystemLoadAverage();

                atomicLoadAverage.set(loadAverage);

                try {
                    Thread.sleep(1000); // TODO: move value to properties
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        if (atomicLoadMonitorThread.compareAndSet(null, loadMonitorThread)) {
            loadMonitorThread.start();
        }
        else {
            throw new IllegalStateException("Simple Load Monitor Service is already running.");
        }
    }

    @Override
    public void stop() {
        final Thread loadMonitorThread = atomicLoadMonitorThread.get();

        if (loadMonitorThread == null) {
            throw new IllegalStateException("Simple Load Monitor Service is not yet running.");
        }

        if (atomicLoadMonitorThread.compareAndSet(loadMonitorThread, null)) {
            loadMonitorThread.interrupt();
            atomicLoadAverage.set(UNKNOWN_LOAD_AVERAGE);
        }
        else {
            // TODO: how should we handle this case? throw exception, or be silent since it "achieved" desired state?
        }
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public double getLoadAverage() {
        return atomicLoadAverage.get();
    }

    @Override
    public boolean isRunning() {
        if (atomicLoadMonitorThread.get() != null) {
            return true;
        }
        else {
            return false;
        }
    }

}
