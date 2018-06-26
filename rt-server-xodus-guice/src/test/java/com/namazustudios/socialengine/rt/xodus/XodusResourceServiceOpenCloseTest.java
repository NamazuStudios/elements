package com.namazustudios.socialengine.rt.xodus;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.*;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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
        return injector.getInstance(ResourceService.class);
    }

    public static final class Module extends AbstractModule {

        private final File scheduler;

        private final File resources;

        public Module(final String base) {
            this.scheduler = new File(base, "scheduler");
            this.resources = new File(base, "resources");
            assertTrue(scheduler.exists() || scheduler.mkdirs(), "Unable to make scheduler database at " + base);
            assertTrue(resources.exists() || resources.mkdirs(), "Unable to make resources database at " + base);
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

            install(new XodusEnvironmentModule()
                .withResourceEnvironmentPath(resources.getAbsolutePath())
                .withSchedulerEnvironmentPath(scheduler.getAbsolutePath()));

            bind(ResourceService.class).to(XodusResourceService.class);
            bind(ResourceLockService.class).to(SimpleResourceLockService.class);
            bind(ResourceLoader.class).toInstance(mockResourceLoader);

        }

    }

}
