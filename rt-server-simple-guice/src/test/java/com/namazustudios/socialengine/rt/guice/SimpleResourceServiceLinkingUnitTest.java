package com.namazustudios.socialengine.rt.guice;

import com.namazustudios.socialengine.rt.ResourceService;
import org.testng.annotations.Guice;

import javax.inject.Inject;

@Guice(modules = SimpleResourceServiceUnitTest.Module.class)
public class SimpleResourceServiceLinkingUnitTest extends AbstractResourceServiceLinkingUnitTest {

    private ResourceService resourceService;

    @Override
    public ResourceService getResourceService() {
        return resourceService;
    }


    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
