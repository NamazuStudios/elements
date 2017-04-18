package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 8/23/15.
 */
public class SimpleInternalContainer extends AbstractSimpleContainer<InternalResource> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleInternalContainer.class);

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
