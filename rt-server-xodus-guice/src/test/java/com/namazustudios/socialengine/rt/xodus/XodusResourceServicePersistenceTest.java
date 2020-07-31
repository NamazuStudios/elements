package com.namazustudios.socialengine.rt.xodus;

import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceIdForNode;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = XodusResourceServicePersistenceTest.Module.class)
public class XodusResourceServicePersistenceTest {

    private static final int MOCK_COUNT = 512;

    private static final Map<Key, ResourceId> originals = new HashMap<>();

    private ResourceService resourceService;

    @DataProvider
    public Object[][] initialDataProvider() {

        final List<Object[]> testData = new ArrayList<>();
        final UUID nodeUuid = randomUUID();

        for (int i = 0; i < 10; ++i) {
            final NodeId nodeId = randomNodeId();
            final ResourceId resourceId = randomResourceIdForNode(nodeId);
            final Path path = new Path(asList("test", resourceId.asString()));
            testData.add(new Object[]{resourceId, path});
        }

        return testData.toArray(new Object[][]{});

    }

    @Test(dataProvider = "initialDataProvider")
    public void testAdd(final ResourceId resourceId, final Path path) throws IOException {

        final byte[] bytes = new byte[MOCK_COUNT];
        ThreadLocalRandom.current().nextBytes(bytes);

        final Resource resource = mock(Resource.class);

        when(resource.getId()).thenReturn(resourceId);
        doAnswer(answer -> {
            final OutputStream os = answer.getArgument(0);
            os.write(bytes);
            originals.put(new Key(bytes), resourceId);
            return null;
        }).when(resource).serialize(any(OutputStream.class));

        getResourceService().addAndReleaseResource(path, resource);

        verify(resource, times(1)).unload();
        verify(resource, times(1)).serialize(any(OutputStream.class));

    }

    @DataProvider
    public Object[][] mockPersistentData() {
        return originals.entrySet()
            .stream()
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "mockPersistentData", dependsOnMethods = "testAdd")
    public void testDeserializeAndDestroy(final Key key, final ResourceId originalId) throws IOException {

        final XodusResource acquired = (XodusResource) getResourceService().getAndAcquireResourceWithId(originalId);

        assertNotNull(originals.get(key), "No resource for key in original table.");
        assertEquals(acquired.getId(), originals.get(key));
        assertEquals(acquired.getId(), originalId);

        getResourceService().release(acquired);
        verify(acquired.getDelegate(), times(1)).unload();
        verify(acquired.getDelegate(), times(1)).serialize(any(OutputStream.class));

        final Resource removed = getResourceService().removeResource(originalId);
        assertEquals(removed, DeadResource.getInstance());

        removed.close();  // Check that the returned resource behaves properly (never throws) which DeadResource should
        removed.unload(); // not do for the closed type.

    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    private static class Key {

        final byte[] bytes;

        public Key(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return Arrays.equals(getBytes(), key.getBytes());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(getBytes());
        }

    }

    public static final class Module extends AbstractModule {

        @Override
        protected void configure() {

            final ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

            when(mockResourceLoader.load(any(InputStream.class))).thenAnswer(invocation -> {

                final byte bytes[] = new byte[MOCK_COUNT];

                ByteStreams.readFully(invocation.getArgument(0), bytes);
                final Key key = new Key(bytes);

                final ResourceId originalId = originals.get(key);
                assertNotNull(originalId, "No resource for bytes.");

                final Resource resource = mock(Resource.class);
                when(resource.getId()).thenReturn(originalId);
                return resource;

            });

            install(new XodusEnvironmentModule().withTempEnvironments());
            bind(NodeId.class).toInstance(randomNodeId());
            bind(ResourceService.class).to(XodusResourceService.class);
            bind(ResourceLockService.class).to(SimpleResourceLockService.class);
            bind(ResourceLoader.class).toInstance(mockResourceLoader);

        }

    }

}
