package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.rt.AbstractSimpleServer;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by patricktwohig on 8/23/15.
 */
public class SimpleInternalServer extends AbstractSimpleServer implements InternalServer {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleInternalServer.class);

    @Inject
    private InternalRequestDispatcher internalRequestDispatcher;

    @Inject
    private ResourceService<InternalResource> resourceService;

    private final Queue<Callable<Void>> eventQueue = new ConcurrentLinkedQueue<>();

    private final Queue<Callable<Void>> requestQueue = new ConcurrentLinkedQueue<>();

    @Override
    protected ServerContext openServerContext() {
        return null;
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
        return resourceService;
    }

    @Override
    public void dispatch(final Request request, final ResponseReceiver responseReceiver) {
        requestQueue.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                try {
                    internalRequestDispatcher.dispatch(request, responseReceiver);
                } catch (Exception ex) {
                    LOG.error("Caught exception mapping request {} to response receiver {}", request, responseReceiver);
                }

                return null;

            }
        });

    }

}
