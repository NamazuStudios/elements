package com.namazustudios.socialengine.rt.remote.watchdog;

import com.namazustudios.socialengine.rt.remote.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.remote.Worker.SCHEDULED_EXECUTOR_SERVICE;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.*;

public class SimpleWatchdogService implements WatchdogService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWatchdogService.class);

    private static final Logger alerts = LoggerFactory.getLogger(format("%s.ALERTS", SimpleWatchdogService.class.getName()));

    private static final long POLL_TIME_MILLISECONDS = MILLISECONDS.convert(5, SECONDS);

    private Worker worker;

    private Set<WorkerWatchdog> workerWatchdogList;

    private ScheduledExecutorService scheduledExecutorService;

    private final AtomicReference<Context> context = new AtomicReference<>();

    @Override
    public void start() {
        final var context = new Context();

        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    @Override
    public void stop() {
        final var context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Not Running.");
        } else {
            context.stop();
        }
    }

    public Set<WorkerWatchdog> getWorkerWatchdogList() {
        return workerWatchdogList;
    }

    public Worker getWorker() {
        return worker;
    }

    @Inject
    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @Inject
    public void setWorkerWatchdogList(Set<WorkerWatchdog> workerWatchdogList) {
        this.workerWatchdogList = workerWatchdogList;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    @Inject
    public void setScheduledExecutorService(@Named(SCHEDULED_EXECUTOR_SERVICE) ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    private class Context {

        private ExecutorService dispatch;

        public void start() {
            dispatch = Executors.newFixedThreadPool(2);

            getScheduledExecutorService().scheduleAtFixedRate(this::poll,
                POLL_TIME_MILLISECONDS,
                POLL_TIME_MILLISECONDS,
                MILLISECONDS);

            logger.info("Started. Using Logger \"{}\" for alerts.", alerts.getName());
        }

        private void poll() {
            final var worker = getWorker();
            getWorkerWatchdogList().forEach(watchdog -> dispatch.submit(() -> watchdog.watch(alerts, worker)));
        }

        public void stop() {
            dispatch.shutdown();
            try {
                if (dispatch.awaitTermination(5, MINUTES)) {
                    logger.info("Shutdown watchdog service.");
                } else {
                    logger.error("Timed out while shutting down watchdog service.");
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted shutting down watchdog.", e);
            }
        }

    }

}
