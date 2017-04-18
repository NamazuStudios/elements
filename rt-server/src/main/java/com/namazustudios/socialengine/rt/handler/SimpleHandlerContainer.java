package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;

/**
 * The simple handler server is responsible for dispatching requests and events to all {@link Resource} instances
 * contained therein.  It accomplishes its task in parallel by dispatching all requests, events, and then
 * finally updating each {@link Resource} in order.
 *
 * Internally, it leverages an instance an {@link ExecutorService} and a {@link CompletionService} to
 * perform all updates in parallel.
 *
 * Created by patricktwohig on 8/22/15.
 */
public class SimpleHandlerContainer extends AbstractSimpleContainer<Handler> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleHandlerContainer.class);

    private ResourceService<Handler> resourceService;

    @Override
    public void shutdown() {}

    @Override
    public ResourceService<Handler> getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService<Handler> resourceService) {
        this.resourceService = resourceService;
    }

}
