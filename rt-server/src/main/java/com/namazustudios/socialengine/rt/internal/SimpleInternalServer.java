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
public class SimpleInternalServer extends AbstractSimpleServer<InternalResource> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleInternalServer.class);

    @Inject
    private InternalRequestDispatcher internalRequestDispatcher;

    private ResourceService<InternalResource> resourceService;

    @Override
    public void shutdown() {
        getResourceService().removeAndCloseAllResources();
    }

    public ResourceService<InternalResource> getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService<InternalResource> resourceService) {
        this.resourceService = resourceService;
    }

}
