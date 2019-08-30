package com.namazustudios.socialengine.rt.xodus;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.xodus.provider.ResourceEnvironmentProvider;
import jetbrains.exodus.env.Environment;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.xodus.XodusResourceService.RESOURCE_ENVIRONMENT;
import static com.namazustudios.socialengine.rt.xodus.provider.ResourceEnvironmentProvider.RESOURCE_ENVIRONMENT_PATH;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class XodusResourceServiceOpenCloseTest {

    @Test
    public void testOpenClose() throws Exception {
        final File base = Files.createTempDir();
        try (final ResourceService rs = open(base.getAbsolutePath())) {}
    }

    @Test
    public void testGetByResourceId() throws Exception {

        final File base = Files.createTempDir();
        final Resource original = newMockResource();
        final Path path = Path.fromComponents("test", randomUUID().toString());

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            rs.addAndReleaseResource(path, original);
            verify(original, times(1)).unload();
            verify(original, times(1)).serialize(any());
        }

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            final Resource loaded = rs.getAndAcquireResourceWithId(original.getId());
            assertNotNull(loaded);
            assertEquals(loaded.getId(), original.getId());
        }

    }

    @Test
    public void testGetByPath() throws Exception {

        final File base = Files.createTempDir();
        final Resource original = newMockResource();
        final Path path = Path.fromComponents("test", randomUUID().toString());

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            rs.addAndReleaseResource(path, original);
            verify(original, times(1)).unload();
            verify(original, times(1)).serialize(any());
        }

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            final Resource loaded = rs.getAndAcquireResourceAtPath(path);
            assertNotNull(loaded);
            assertEquals(loaded.getId(), original.getId());
        }

    }

    @Test
    public void testGetByResourceIdLeaked() throws Exception {

        final File base = Files.createTempDir();
        final Resource original = newMockResource();
        final Path path = Path.fromComponents("test", randomUUID().toString());

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            rs.addAndAcquireResource(path, original);
        }

        verify(original, times(1)).unload();
        verify(original, times(1)).serialize(any());

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            final Resource loaded = rs.getAndAcquireResourceWithId(original.getId());
            assertNotNull(loaded);
            assertEquals(loaded.getId(), original.getId());
        }

    }

    @Test
    public void testGetByPathLeaked() throws Exception {

        final File base = Files.createTempDir();
        final Resource original = newMockResource();
        final Path path = Path.fromComponents("test", randomUUID().toString());

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            rs.addAndAcquireResource(path, original);
        }

        verify(original, times(1)).unload();
        verify(original, times(1)).serialize(any());

        try (final ResourceService rs = open(base.getAbsolutePath())) {
            final Resource loaded = rs.getAndAcquireResourceAtPath(path);
            assertNotNull(loaded);
            assertEquals(loaded.getId(), original.getId());
        }

    }

    private Resource newMockResource() throws IOException {

        final Resource mock = mock(Resource.class);
        final ResourceId resourceId = new ResourceId();

        when(mock.getId()).thenReturn(resourceId);
        doAnswer(invocation -> {
            final OutputStream os = invocation.getArgument(0);
            final byte[] bytes = resourceId.asString().getBytes(UTF_8);
            os.write(bytes);
            return null;
        }).when(mock).serialize(any());

        return mock;

    }

    private ResourceService open(final String base) {
        final Injector injector = Guice.createInjector(new Module(base));
        final ResourceService rs = injector.getInstance(ResourceService.class);
        rs.start();
        return rs;
    }

    public static final class Module extends AbstractModule {

        private final String base;

        public Module(final String base) {
            this.base = base;
        }

        @Override
        protected void configure() {
            final ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

            when(mockResourceLoader.load(any())).thenAnswer(invocation -> {

                final InputStream is = invocation.getArgument(0);
                final byte[] bytes = ByteStreams.toByteArray(is);

                final ResourceId resourceId = new ResourceId(new String(bytes, UTF_8));
                final Resource resource = mock(Resource.class);
                when(resource.getId()).thenReturn(resourceId);

                return resource;

            });

            bind(ResourceService.class).to(XodusResourceService.class);
            bind(ResourceLockService.class).to(SimpleResourceLockService.class);
            bind(ResourceLoader.class).toInstance(mockResourceLoader);

            bind(Environment.class)
                .annotatedWith(named(RESOURCE_ENVIRONMENT))
                .toProvider(ResourceEnvironmentProvider.class)
                .asEagerSingleton();

            bind(String.class)
                .annotatedWith(named(RESOURCE_ENVIRONMENT_PATH))
                .toInstance(base);

        }

    }

}
