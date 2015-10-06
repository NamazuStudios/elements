package com.namazustudios.socialengine.rt;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by patricktwohig on 8/23/15.
 */
public abstract class AbstractSimpleServer<ResourceT extends Resource> implements Server<ResourceT>, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSimpleServer.class);

    /**
     * Specifies the max number of updates per second the server will attempt to make.  Setting to zero
     * will not throttle the update rate at all.  This may not be desirable, especially when the server
     * is not under load updates will be processed so quickly that Resources may dispatch zero values
     * for time deltas.
     */
    public static final String MAX_UPDATES_PER_SECOND = "com.namazustudios.socialengine.rt.AbstractSimpleServer.maxUpdatesPerSecond";

    /**
     * This is the maximum number of requests that the server will accept before giving up and processing the next frame.
     */
    public static final String MAX_REQUESTS = "com.namazustudios.socialengine.rt.AbstractSimpleServer.maxRequests";

    /**
     * This is the maximum number of requests that server will process before giving up and processing the next frame.
     */
    public static final String MAX_EVENTS = "com.namazustudios.socialengine.rt.AbstractSimpleServer.maxEvents";

    /**
     * The SimpleEdgeServer uses an {@link ExecutorService} to process requests and dispath
     * events to the various {@link Resource}s.
     */
    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.AbstractSimpleServer.executorService";

    @Inject
    @Named(MAX_REQUESTS)
    private int maxRequests;

    @Inject
    @Named(MAX_EVENTS)
    private int maxEvents;

    @Inject
    @Named(MAX_UPDATES_PER_SECOND)
    private int maxUpdatesPerSecond;

    @Inject
    @Named(EXECUTOR_SERVICE)
    private ExecutorService executorService;

    @Inject
    private ObservationEventReceiverMap observationEventReceiverMap;

    @Inject
    private ResourceService<ResourceT> resourceService;

    private final AtomicBoolean running = new AtomicBoolean();

    @Override
    public void post(final Callable<Void> operation) {
        getEventQueue().add(operation);
    }

    @Override
    public <PayloadT> Observation observe(final Path path,
                                          final String name,
                                          final EventReceiver<PayloadT> eventReceiver) {
        final EventReceiver<PayloadT> eventReceiverWrapper = wrap(eventReceiver, path);
        return observationEventReceiverMap.subscribe(path, name, eventReceiverWrapper);
    }

    @Override
    public <PayloadT> List<Subscription> subscribe(final Path path,
                                                   final String name,
                                                   final EventReceiver<PayloadT> eventReceiver) {

        final List<Subscription> subscriptionList = new ArrayList<>();
        final EventReceiver<PayloadT> eventReceiverWrapper = wrap(eventReceiver, path);

        if (path.isWildcard()) {
            for (final Resource resource : getResourceService().getResources(path)) {
                final Subscription subscription = resource.subscribe(name, eventReceiverWrapper);
                subscriptionList.add(subscription);
            }
        } else {
            final Resource resource = getResourceService().getResource(path);
            final Subscription subscription =  resource.subscribe(name, eventReceiverWrapper);
            subscriptionList.add(subscription);
        }

        return subscriptionList;

    }

    private <PayloadT> EventReceiver<PayloadT> wrap(final EventReceiver<PayloadT> eventReceiver, final Path path) {
        return new EventReceiver<PayloadT>() {

            @Override
            public Class<PayloadT> getEventType() {
                return eventReceiver.getEventType();
            }

            @Override
            public void receive(final Event event) {
                getEventQueue().add(new Callable<Void>() {

                    @Override
                    public Void call() {
                        try {
                            eventReceiver.receive(event);
                        } catch (Exception ex) {
                            LOG.error("Caught exception for receiver {} event {}", eventReceiver, event);
                        }

                        return null;
                    }

                    @Override
                    public String toString() {
                        return "EventModel receiver " + eventReceiver + "for event " + getEventType() + " at path" + path;
                    }

                });
            }

        };
    }

    @Override
    public void run() {

        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already running.");
        }

        final Thread runnerThread = Thread.currentThread();

        try (final ServerContext context = openServerContext()) {

            LOG.info("Starting server main loop for server {}", this);

            double movingAverageMillis = 0;
            final Stopwatch logTimer = Stopwatch.createStarted();
            final Stopwatch updateTimer = Stopwatch.createStarted();
            final long maxSleepTime = maxUpdatesPerSecond == 0 ? 0 : Math.round(1000.0 / (double)maxUpdatesPerSecond);

            do {

                dispatchQueue(getRequestQueue(), maxRequests);
                dispatchQueue(getEventQueue(), maxEvents);
                doUpdate();

                final long elapsed = Math.round(updateTimer.elapsed(TimeUnit.MILLISECONDS));
                movingAverageMillis += elapsed;
                movingAverageMillis /= 2;

                if (elapsed < maxSleepTime) {
                    Thread.sleep(maxSleepTime - elapsed);
                }

                updateTimer.reset();
                updateTimer.start();

                if (logTimer.elapsed(TimeUnit.SECONDS) >= 5) {
                    LOG.info("Average server tick time {}ms", movingAverageMillis);
                    logTimer.reset();
                    logTimer.start();
                }

            } while (running.get() && !runnerThread.isInterrupted());

            LOG.info("Main server loop stopping for server {}", this);

        } catch (InterruptedException ex) {
            LOG.info("Server thread interrupted.  Stopping.", ex);
            return;
        }

    }

    private void dispatchQueue(final Queue<Callable<Void>> queue, final int max) throws InterruptedException {

        final Set<Future<Void>> futureSet = new HashSet<>();
        final CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);

        final List<Callable<Void>> operationList = getOperationsForUpdate(queue, max);
        for (final Callable<Void> operation : operationList) {
            final Future<Void> future = completionService.submit(operation);
            futureSet.add(future);
        }

        while (!futureSet.isEmpty()) {

            final Future<Void> completed = completionService.take();

            if (completed != null) {
                getAndLogResult(completed);
                futureSet.remove(completed);
            } else {
                break;
            }

        }

    }

    private List<Callable<Void>> getOperationsForUpdate(final Queue<Callable<Void>> queue, final int max) {

        final List<Callable<Void>> operationList = new ArrayList<>();

        Callable<Void> operation = queue.poll();

        for (int i = 0; i < max && operation != null; ++i) {
            operationList.add(operation);
            operation = queue.poll();
        }

        return operationList;
    }

    private void doUpdate() throws InterruptedException {

        final Set<Future<Void>> futureSet = new HashSet<>();
        final CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        for (final Resource edgeResource : getResourceService().getResources()) {

            final Future<Void> future = completionService.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    edgeResource.onUpdate();
                    return null;
                }

                @Override
                public String toString() {
                    return edgeResource.toString();
                }

            });

            futureSet.add(future);

        }

        while (!futureSet.isEmpty()) {

            final Future<Void> completed = completionService.take();

            if (completed != null) {
                getAndLogResult(completed);
                futureSet.remove(completed);
            } else {
                break;
            }

        }

    }

    private <T> void getAndLogResult(final Future<T> future) throws InterruptedException {
        try {
            final T t = future.get();
            LOG.trace("Future exited with value {}", t);
        } catch (ExecutionException ex) {
            LOG.error("Caught execution exception for future.", ex);
        }
    }

    /**
     * Hands the {@link Event} to the server's {@link ObservationEventReceiverMap}.
     *
     * @param event
     */
    public void postToObservers(final Event event) {
        post(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                observationEventReceiverMap.dispatch(event);
                return null;
            }
        });
    }

    /**
     * Signals the currently running server to complete work and gracefully shut down.  The server
     * will complete work for the current tick and then shut down.
     */
    public void shutdown() {
        running.set(false);
    }

    /**
     * Returns a new {@link ServerContext}, which will exist as long as the server
     * is running.  Upon shutdown, the returned instance is closed
     *
     * @return a new {@link ServerContext}
     */
    protected abstract ServerContext openServerContext();

    /**
     * Gets a {@link Queue} used to enqueue  events.  The returned instance must be thread-safe.
     *
     * @return the event queue
     */
    protected abstract Queue<Callable<Void>> getEventQueue();

    /**
     * Gets a {@link Queue} used to enqueue  requests.  The returned instance must be thread-safe.
     *
     * @return the event queue
     */
    protected abstract Queue<Callable<Void>> getRequestQueue();

    /**
     * Gets the {@link ResourceService<?>} that is managed by this server.
     *
     * @return the {@link ResourceService} managed by this server.
     */
    protected abstract ResourceService<?> getResourceService();

    /**
     * Used to keep track of the server while it is up.  This may do anything required during
     * the server's lifecycle.  Actions such as loading bootstrap resources should be performed
     * when creating this process and then shut down later.
     */
    protected interface ServerContext extends AutoCloseable {

        /**
         * Releases any long-term resources used by this server.
         */
        @Override
        void close();

    }

}
