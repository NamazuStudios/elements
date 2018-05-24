package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceUnitTest;
import org.mockito.Mockito;
import org.testng.annotations.Guice;

import javax.inject.Inject;

import static org.mockito.Mockito.*;

@Guice(modules = XodusResourceServiceUnitTest.Module.class)
public class XodusResourceServiceUnitTest extends AbstractResourceServiceUnitTest {

    private ResourceService resourceService;

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

            install(new XodusServicesModule().withSchedulerThreads(1));
            install(new XodusEnvironmentModule().withTempEnvironment());

            final AssetLoader mockAssetLoader = mock(AssetLoader.class);
            bind(AssetLoader.class).toInstance(mockAssetLoader);

            final ResourceLoader resourceLoader = mock(ResourceLoader.class);
            bind(ResourceLoader.class).toInstance(resourceLoader);

        }
    }

}
