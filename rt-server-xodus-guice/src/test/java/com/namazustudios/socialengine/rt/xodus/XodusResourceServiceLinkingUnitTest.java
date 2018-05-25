package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceLinkingUnitTest;
import org.testng.annotations.Guice;

import javax.inject.Inject;

@Guice(modules = XodusResourceServiceUnitTest.Module.class)
public class XodusResourceServiceLinkingUnitTest extends AbstractResourceServiceLinkingUnitTest {

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
