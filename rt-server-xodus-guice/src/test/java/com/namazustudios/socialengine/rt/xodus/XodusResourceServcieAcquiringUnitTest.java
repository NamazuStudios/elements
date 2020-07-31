package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceAcquiringUnitTest;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.mockito.Mockito;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.io.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

@Guice(modules = XodusResourceServiceReleasingUnitTest.Module.class)
public class XodusResourceServcieAcquiringUnitTest extends AbstractResourceServiceAcquiringUnitTest {

    private ResourceService resourceService;

    @Override
    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public Resource getMockResource(final ResourceId resourceId) {
        return doGetMockResource(resourceId);
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {

            install(new XodusServicesModule().withSchedulerThreads(1));
            install(new XodusEnvironmentModule().withTempEnvironments());

            final AssetLoader mockAssetLoader = mock(AssetLoader.class);
            bind(AssetLoader.class).toInstance(mockAssetLoader);

            final ResourceLoader resourceLoader = mock(ResourceLoader.class);

            doAnswer(a -> {
                fail("No attempt to load resource should be made for this test.");
                return null;
            }).when(resourceLoader).load(any(InputStream.class));

            bind(ResourceLoader.class).toInstance(resourceLoader);

        }
    }

    private static Resource doGetMockResource(final ResourceId resourceId) {

        final Resource resource = Mockito.mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);

        try {
            doAnswer(a -> {
                fail("No attempt to persist resource shoudl be mae for this test.");
                return null;
            }).when(resource).serialize(any(OutputStream.class));
        } catch (IOException e) {
            // Should never happen in test code unless something is really wrong
            throw new UncheckedIOException(e);
        }

        return resource;

    }

}
