package com.namazustudios.socialengine.rt;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by patricktwohig on 8/23/15.
 */
public abstract class AbstractSimpleServer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSimpleServer.class);

    /**
     * This is the maximum number of requests that the server will accept before giving up and processing the next frame.
     */
    public static final String MAX_REQUESTS = "com.namazustudios.socialengine.rt.edge.SimpleEdgeServer.maxRequests";

    /**
     * This is the maximum number of requests that server will process before giving up and processing the next frame.
     */
    public static final String MAX_EVENTS = "com.namazustudios.socialengine.rt.edge.SimpleEdgeServer.maxRequests";

    /**
     * This is the amount of time (in seconds) that all {@link Resource}s should take to process events,
     * request handlers, or the call to {@link Resource#onUpdate()} method.  Resources that are hogging system
     * time will be logged as problematic.  It is a good idea to set this to a low number, such as 0.5 seconds
     * to encourage resource developers to write responsive code.
     */
    public static final String RESOURCE_TIMEOUT = "com.namazustudios.socialengine.rt.edge.SimpleEdgeServer.resourceTimeout";

    /**
     * The SimpleEdgeServer uses an {@link ExecutorService} to process requests and dispath
     * events to the various {@link Resource}s.
     */
    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.edge.SimpleEdgeServer.executorService";

    /**
     * The SimpleEdgeServer must have some {@link Resource}s it will add when the server first starts up.  This
     * must be specified as a {@link Map} of stings to {@link Resource} values with the key as the path and the
     * value as the resource at the path.
     */
    public static final String BOOTSTRAP_RESOURCES = "com.namazustudios.socialengine.rt.edge.SimpleEdgeServer.maxRequests";

    private final AtomicReference<Thread> runnerThreadAtomicReference = new AtomicReference<>();

    @Inject
    @Named(MAX_REQUESTS)
    private int maxRequests;

    @Inject
    @Named(MAX_EVENTS)
    private int maxEvents;

    @Inject
    @Named(RESOURCE_TIMEOUT)
    private double resourceTimeout;

    @Inject
    @Named(EXECUTOR_SERVICE)
    private ExecutorService executorService;

    public <PayloadT> Subscription subscribe(final String path,
                                             final String name,
                                             final EventReceiver<PayloadT> eventReceiver) {

        final EventReceiver<PayloadT> eventReceiverWrapper = new EventReceiver<PayloadT>() {

            @Override
            public Class<PayloadT> getEventType() {
                return eventReceiver.getEventType();
            }

            @Override
            public void receive(final String path, final String name, final PayloadT event) {
                getEventQueue().add(new Callable<Void>() {

                    @Override
                    public Void call() {
                        try {
                            eventReceiver.receive(path, name, event);
                        } catch (Exception ex) {
                            LOG.error("Caught exception for receiver {} at path {}", eventReceiver, path);
                        }

                        return null;
                    }

                    @Override
                    public String toString() {
                        return "EventModel receiver " + eventReceiver + "for event " + event + " at path" + path;
                    }

                });
            }

        };

        final Resource edgeResource = getResourceService().getResource(path);
        return edgeResource.subscribe(name, eventReceiverWrapper);

    }

    @Override
    public void run() {

        final Thread runnerThread = Thread.currentThread();

        if (!runnerThreadAtomicReference.compareAndSet(null, Thread.currentThread())) {
            throw new IllegalStateException("Server already running thread.");
        }

        try (final ServerContext context = openServerContext()) {

            do {
                dispatchQueue(getRequestQueue(), maxRequests);
                dispatchQueue(getEventQueue(), maxEvents);
                doUpdate();
            } while (!runnerThread.isInterrupted());

        } catch (InterruptedException ex) {
            LOG.info("Server thread interrupted.  Stopping.", ex);
            return;
        } finally {
            if (!runnerThreadAtomicReference.compareAndSet(runnerThread, null)) {
                throw new IllegalStateException("Server abandoned running thread.");
            }
        }

    }

    private void dispatchQueue(final Queue<Callable<Void>> queue, final int max) throws InterruptedException {

        final Set<Future<Void>> futureSet = new HashSet<>();
        final CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);

        Callable<Void> operation = queue.poll();

        for (int i = 0; i < max && operation != null; ++i) {
            final Future<Void> future = completionService.submit(operation);
            futureSet.add(future);
            operation = queue.poll();
        }

        final Stopwatch timeoutStopwatch = Stopwatch.createStarted();
        final long resourceTimeoutInMilliseconds = Math.round(Constants.MILLISECONDS_PER_SECOND * resourceTimeout);

        do {

            final long toWait = Math.max(0, resourceTimeoutInMilliseconds -
                                            timeoutStopwatch.elapsed(TimeUnit.MILLISECONDS));

            final Future<Void> completed = completionService.poll(toWait, TimeUnit.MILLISECONDS);
            futureSet.remove(completed);

        } while (timeoutStopwatch.elapsed(TimeUnit.MILLISECONDS) <= resourceTimeoutInMilliseconds);

        for (final Future<Void> future : futureSet) {
            LOG.warn("Canceling future due to timeout {}." + future);
            future.cancel(false);
        }

        do {
            final Future<Void> future = completionService.take();
            futureSet.remove(future);
        } while (!futureSet.isEmpty());

    }

    private void doUpdate() throws InterruptedException {

        final Set<Future<Void>> futureSet = new HashSet<>();

        final CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);

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

        final Stopwatch timeoutStopwatch = Stopwatch.createStarted();
        final long resourceTimeoutInMilliseconds = Math.round(Constants.MILLISECONDS_PER_SECOND * resourceTimeout);

        do {

            final long toWait = Math.max(0, resourceTimeoutInMilliseconds -
                    timeoutStopwatch.elapsed(TimeUnit.MILLISECONDS));

            final Future<Void> completed = completionService.poll(toWait, TimeUnit.MILLISECONDS);
            futureSet.remove(completed);

        } while (timeoutStopwatch.elapsed(TimeUnit.MILLISECONDS) <= resourceTimeoutInMilliseconds);

        for (final Future<Void> future : futureSet) {
            LOG.warn("Canceling future due to timeout {}." + future);
            future.cancel(true);
        }

        do {
            final Future<Void> future = completionService.take();
            futureSet.remove(future);
        } while (!futureSet.isEmpty());

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
