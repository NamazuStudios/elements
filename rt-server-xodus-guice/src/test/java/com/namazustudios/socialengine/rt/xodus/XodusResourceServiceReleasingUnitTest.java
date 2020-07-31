package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.guice.AbstractResourceServiceReleasingUnitTest;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.mockito.Mockito;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.io.*;

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.*;

@Guice(modules = XodusResourceServiceReleasingUnitTest.Module.class)
public class XodusResourceServiceReleasingUnitTest extends AbstractResourceServiceReleasingUnitTest {

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
                final InputStream is = a.getArgument(0);
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                for (int b = is.read(); b >= 0; b = is.read()) bos.write((byte)b);
                final String resourceIdString = new String(bos.toByteArray(), UTF_8);
                final ResourceId resourceId = resourceIdFromString(resourceIdString);
                return doGetMockResource(resourceId);
            }).when(resourceLoader).load(any(InputStream.class));

            bind(NodeId.class).toInstance(randomNodeId());
            bind(ResourceLoader.class).toInstance(resourceLoader);

        }
    }

    private static Resource doGetMockResource(final ResourceId resourceId) {

        final Resource resource = Mockito.mock(Resource.class);
        when(resource.getId()).thenReturn(resourceId);

        try {
            doAnswer(a -> {
                final OutputStream os = a.getArgument(0);
                final byte[] bytes = resourceId.asString().getBytes(UTF_8);
                os.write(bytes);
                return null;
            }).when(resource).serialize(any(OutputStream.class));
        } catch (IOException e) {
            // Should never happen in test code unless something is really wrong
            throw new UncheckedIOException(e);
        }

        return resource;

    }

}
