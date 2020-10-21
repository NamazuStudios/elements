package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.mockito.Mockito;
import org.testng.annotations.Guice;

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;

@Guice(modules = SimpleResourceServiceReleasingUnitTest.Module.class)
public class SimpleResourceServiceReleasingUnitTest extends AbstractResourceServiceReleasingUnitTest {

    protected ResourceService resourceService;

    @Override
    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            install(new SimpleServicesModule().withSchedulerThreads(1));

            final AssetLoader mockAssetLoader = Mockito.mock(AssetLoader.class);
            bind(AssetLoader.class).toInstance(mockAssetLoader);

            final ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
            bind(ResourceLoader.class).toInstance(resourceLoader);

            bind(NodeId.class).toInstance(randomNodeId());

        }

    }

}
