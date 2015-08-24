package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.rt.AbstractSimpleServer;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.edge.EdgeResource;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by patricktwohig on 8/23/15.
 */
public class SimpleInternalServer extends AbstractSimpleServer implements InternalServer {

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
    public void dispatch(Request request, ResponseReceiver responseReceiver) {

    }

}
