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
public class SimpleInternalServer extends AbstractSimpleServer<InternalResource> implements InternalServer {

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
    protected ResourceService<?> getResourceService() {
        return resourceService;
    }

    @Override
    public InternalResource retain(Path path) {

        final InternalResource internalResource = resourceService.getResource(path);

        try {
            internalResource.retain();
        } catch (InternalResource.ZeroReferenceCountException ex) {
            // We do nothing here because this resource is essentially slated for removal
            // and we just happen to have gotten it while it was in the state of being
            // removed.
            throw new NotFoundException(ex);
        }

        return internalResource;

    }

    @Override
    public InternalResource retainOrAddResourceIfAbsent(final Path path,
                                                        final ResourceInitializer<InternalResource> resourceInitializer) {

        do {

            final ResourceService.AtomicOperationTuple<InternalResource> atomicOperationTuple;
            atomicOperationTuple = resourceService.addResourceIfAbsent(path, resourceInitializer);

            if (!atomicOperationTuple.isNewlyAdded()) {
                try {
                    atomicOperationTuple.getResource().retain();
                } catch (InternalResource.ZeroReferenceCountException ex) {
                    // This is probably rare, but if there is add/remove contention over the resource
                    // then we will have to attempt the operation again.
                    continue;
                }
            }

            return atomicOperationTuple.getResource();

        } while (true);

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

    @Override
    public Iterable<InternalResource> getResources() {
        return resourceService.getResources();
    }

    @Override
    public InternalResource getResource(Path path) {
        return resourceService.getResource(path);
    }

    @Override
    public Iterable<InternalResource> getResources(Path path) {
        return resourceService.getResources(path);
    }

    @Override
    public void addResource(Path path, InternalResource resource) {
        resourceService.addResource(path, resource);
    }

    @Override
    public void moveResource(Path source, Path destination) {
        resourceService.moveResource(source, destination);
    }

    @Override
    public void removeAllResources() {
        resourceService.removeAllResources();
    }

    @Override
    public InternalResource removeResource(Path path) {
        return resourceService.removeResource(path);
    }
    
    private class InternalServerContext implements ServerContext {

        public InternalServerContext() {
            LOG.info("Bootstrapping Internal server {} ", SimpleInternalServer.this);
        }

        @Override
        public void close() {
            resourceService.removeAllResources();
        }

    }

}
