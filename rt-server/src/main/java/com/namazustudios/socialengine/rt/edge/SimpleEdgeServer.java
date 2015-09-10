package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.*;

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
public class SimpleEdgeServer extends AbstractSimpleServer implements EdgeServer {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEdgeServer.class);

    @Inject
    private EdgeRequestDispatcher edgeRequestDispatcher;

    @Inject
    private ResourceService<EdgeResource> edgeResourceService;

    @Inject
    @Named(EdgeServer.BOOTSTRAP_RESOURCES)
    private Map<Path, EdgeResource> bootstrapResources;

    private final Queue<Callable<Void>> eventQueue = new ConcurrentLinkedQueue<>();

    private final Queue<Callable<Void>> requestQueue = new ConcurrentLinkedQueue<>();

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
    protected ServerContext openServerContext() {
        return new EdgeServerContext();
    }

    @Override
    protected Queue<Callable<Void>> getEventQueue() {
        return eventQueue;
    }

    @Override
    protected Queue<Callable<Void>> getRequestQueue() {
        return requestQueue;
    }

    @Override
    protected ResourceService<?> getResourceService() {
        return edgeResourceService;
    }

    private class EdgeServerContext implements ServerContext {

        public EdgeServerContext() {

            SimpleEdgeServer.LOG.info("Bootstrapping server.");

            for (final Map.Entry<Path, EdgeResource> entry : bootstrapResources.entrySet()) {
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
