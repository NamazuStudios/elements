package com.namazustudios.socialengine.rt.edge;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The simple edge server is responsible for dispatching requests and events to all {@link Resource} instances
 * contained therein.  It accomplishes its task in parallel by dispatching all requests, events, and then
 * finally updating each {@link Resource} in order.
 *
 * Internally, it leverages an instance an {@link ExecutorService} and a {@link CompletionService} to
 * perform all updates in parallel.
 *
 * Created by patricktwohig on 8/22/15.
 */
public class SimpleEdgeServer implements EdgeServer {

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

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEdgeServer.class);

    @Inject
    private EdgeRequestDispatcher edgeRequestDispatcher;

    @Inject
    private ResourceService<EdgeResource> edgeResourceService;

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

    @Inject
    @Named(BOOTSTRAP_RESOURCES)
    private Map<String, EdgeResource> bootstrapResources;

    private final Queue<Callable<Void>> eventQueue = new ConcurrentLinkedQueue<>();

    private final Queue<Callable<Void>> requestQueue = new ConcurrentLinkedQueue<>();

    private final AtomicReference<Thread> runnerThreadAtomicReference = new AtomicReference<>();

    @Override
    public <PayloadT> Subscription subscribe(final String path,
                                             final String name,
                                             final EventReceiver<PayloadT> eventReceiver) {

        final EventReceiver<PayloadT> eventReceiverWrapper = new EventReceiver<PayloadT>() {

            @Override
            public Class<PayloadT> getEventType() {
                return eventReceiver.getEventType();
            }

            @Override
            public void receive(final PayloadT event) {
                eventQueue.add(new Callable<Void>() {

                    @Override
                    public Void call() {
                        try {
                            eventReceiver.receive(event);
                        } catch (Exception ex) {
                            LOG.error("Caught exception for receiver {} at path {}", eventReceiver, path);
                        }

                        return null;
                    }

                    @Override
                    public String toString() {
                        return "Event receiver " + eventReceiver + "for event " + event + " at path" + path;
                    }

                });
            }

        };

        final EdgeResource edgeResource = edgeResourceService.getResource(path);
        return edgeResource.subscribe(name, eventReceiverWrapper);

    }

    @Override
    public void dispatch(final EdgeClient edgeClient,
                         final Request request,
                         final ResponseReceiver responseReceiver) {
        requestQueue.add(new Callable<Void>() {

            @Override
            public Void call() {

                try {
                    edgeRequestDispatcher.dispatch(edgeClient, request, responseReceiver);
                } catch (Exception ex) {
                    LOG.error("Caught exception handling request {} for client {} ", request, edgeClient);
                }

                return null;
            }

            @Override
            public String toString() {
                return "Request dispatcher for reqquest " + request + " for receiver " + responseReceiver;
            }

        });
    }

    @Override
    public void run() {

        final Thread runnerThread = Thread.currentThread();

        if (!runnerThreadAtomicReference.compareAndSet(null, Thread.currentThread())) {
            throw new IllegalStateException("Server already running thread.");
        }

        try (final ServerContext context = new ServerContext()) {

            do {
                dispatchQueue(requestQueue, maxRequests);
                dispatchQueue(eventQueue, maxEvents);
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

        for (final EdgeResource edgeResource : edgeResourceService.getResources()) {

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

    private class ServerContext implements AutoCloseable {

        public ServerContext() {

            LOG.info("Bootstrapping server.");

            for (final Map.Entry<String, EdgeResource> entry : bootstrapResources.entrySet()) {
                edgeResourceService.addResource(entry.getKey(), entry.getValue());
                LOG.info("Bootstrapped resource {} at path {}", entry.getValue(), entry.getKey());
            }

        }

        @Override
        public void close() {
            edgeResourceService.removeAllResources();
        }

    }

}
