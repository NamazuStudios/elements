package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;

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
public class SimpleEdgeContainer extends AbstractSimpleContainer<EdgeResource> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEdgeContainer.class);

    private ResourceService<EdgeResource> resourceService;

    @Override
    public void shutdown() {}

    @Override
    public ResourceService<EdgeResource> getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService<EdgeResource> resourceService) {
        this.resourceService = resourceService;
    }

}
