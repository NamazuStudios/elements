package dev.getelements.elements.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dev.getelements.elements.rt.AssetLoader;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.id.NodeId;
import org.mockito.Mockito;
import org.testng.annotations.Guice;

import static dev.getelements.elements.rt.id.NodeId.randomNodeId;

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

            install(new SimpleServicesModule());
            install(new SimpleExecutorsModule().withDefaultSchedulerThreads());

            final AssetLoader mockAssetLoader = Mockito.mock(AssetLoader.class);
            bind(AssetLoader.class).toInstance(mockAssetLoader);

            final ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
            bind(ResourceLoader.class).toInstance(resourceLoader);

            bind(NodeId.class).toInstance(randomNodeId());

        }

    }

}
