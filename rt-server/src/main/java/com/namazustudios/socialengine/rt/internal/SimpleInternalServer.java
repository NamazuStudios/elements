package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;
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
        return new InternalServerContext();
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
    public void dispatch(final Request request, final ResponseReceiver responseReceiver) {
        requestQueue.add(() -> {

            try {
                internalRequestDispatcher.dispatch(request, responseReceiver);
            } catch (Exception ex) {
                LOG.error("Caught exception mapping request {} to response receiver {}", request, responseReceiver);
            }

            return null;

        });

    }


    private class InternalServerContext implements ServerContext {

        public InternalServerContext() {
            LOG.info("Bootstrapping Internal server {} ", SimpleInternalServer.this);
        }

        @Override
        public void close() {
            resourceService.removeAndCloseAllResources();
        }

    }

}
