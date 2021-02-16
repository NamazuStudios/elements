package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;
import static com.namazustudios.socialengine.rt.Context.REMOTE;
import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

@Guice(modules = ErisPersistenceTestModule.class)
public class TestCoreErisPersistenceChannels {

    private static final Logger logger = LoggerFactory.getLogger(TestCoreErisPersistenceChannels.class);

    private static TestTemporaryFiles testTemporaryFiles = new TestTemporaryFiles();

    private ResourceLoader resourceLoader;

    @AfterSuite
    public static void deleteTempFiles() {
        testTemporaryFiles.deleteTempFiles();
    }

    @DataProvider
    public static Object[][] allLuaResources() {
        return TestCoreErisPersistenceStreams.allLuaResources();
    }

    @Test(dataProvider = "allLuaResources", invocationCount = 10)
    public void testPersistUnpersist(final String moduleName) throws IOException {

        logger.debug("Testing Persistence for {}", moduleName);

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var wbc = open(tempFile, WRITE);
             final var resource = getResourceLoader().load(moduleName)) {
            resource.setVerbose(true);
            resource.serialize(wbc);
        }

        try (final var rbc = open(tempFile, READ);
             final var resource = getResourceLoader().load(rbc)) {
            logger.debug("Successfully loaded {}", resource);
        }

    }

    @Test
    public void testIocIsRestoredAfterUnpersist() throws IOException {

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var wbc = open(tempFile, WRITE);
             final var resource = getResourceLoader().load("test.ioc_resolve")) {

            final var result = new AtomicReference<>();
            final var exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(wbc);

        }

        try (final var rbc = open(tempFile, READ);
             final var resource = getResourceLoader().load(rbc, true)) {

            final var result = new AtomicReference<>();
            final var exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

        }

    }

    @Test
    public void testIocProviderIsRestoredAfterUnpersist() throws IOException {

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var wbc = open(tempFile, WRITE);
             final var resource = getResourceLoader().load("test.ioc_resolve")) {

            final var result = new AtomicReference<>();
            final var exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(wbc);

        }

        try (final var rbc = open(tempFile, READ);
             final var resource = getResourceLoader().load(rbc, true)) {

            final var result = new AtomicReference<>();
            final var exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

        }

    }

    @Test(threadPoolSize = 100, invocationCount = 100)
    public void testUpvaluesArePersisted() throws Exception {

        final var tempFile = testTemporaryFiles.createTempFile();
        final var original = new DummyObject();

        try (final var wbc = open(tempFile, WRITE);
             final var resource = getResourceLoader().load("test.persist_upvalue")) {

            resource.getMethodDispatcher("set_upval")
                    .params(original)
                    .dispatch(o -> assertNull(o), ex -> fail("No error expected."));

            resource.getTasks();
            resource.serialize(wbc);

        }

        var toRead = tempFile;
        var toWrite = testTemporaryFiles.createTempFile();

        for (int i = 0; i < 100; ++i) try (final FileChannel rbc = open(toRead, READ);
                                           final Resource resource = getResourceLoader().load(rbc);
                                           final WritableByteChannel wbc = open(toWrite, WRITE)) {

            resource.getMethodDispatcher("assert_upval")
                    .params()
                    .dispatch(o -> assertEquals(original, o), ex -> fail("No error expected."));

            resource.serialize(wbc);

            toRead = toWrite;
            toWrite = testTemporaryFiles.createTempFile();

        }

    }

    @Test
    public void testCoroutinesAreRestored() throws IOException {

        final TaskId taskId;
        final Set<TaskId> taskIdSet;

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var wbc = open(tempFile, WRITE);
             final var resource = getResourceLoader().load("test.simple_yield")) {

            final AtomicReference<String> taskIdAtomicReference = new AtomicReference<>();

            resource.getMethodDispatcher("do_simple_yield")
                    .params()
                    .dispatch(o -> taskIdAtomicReference.set(o.toString()), ex -> fail("Should not happen."));

            assertNotNull(taskIdAtomicReference.get(), "Expected Task ID.");
            taskId = new TaskId(taskIdAtomicReference.get());

            taskIdSet = resource.getTasks();
            assertEquals(taskIdSet.size(), 1);
            assertTrue(taskIdSet.contains(taskId), "TaskID is not in TaskID Set.");

            resource.setVerbose(true);
            resource.serialize(wbc);

        }

        try (final var rbc = open(tempFile, READ);
             final var resource = getResourceLoader().load(rbc, true)) {
            assertEquals(resource.getTasks(), taskIdSet);
            final Set<TaskId> restoredIdSet = resource.getTasks();
            assertEquals(restoredIdSet, taskIdSet);
            resource.resumeFromScheduler(taskId, 0);
        }

    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            install(new LuaModule());

            final NodeId nodeId = randomNodeId();
            bind(NodeId.class).toInstance(nodeId);

            // Types backed by actual implementations
            bind(IocResolver.class).to(GuiceIoCResolver.class).asEagerSingleton();
            bind(AssetLoader.class).to(ClasspathAssetLoader.class).asEagerSingleton();
            bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();

            // Types that are mocks

            bind(Client.class).toInstance(mock(Client.class));

            bind(Context.class).annotatedWith(named(LOCAL)).toInstance(mock(Context.class));
            bind(Context.class).annotatedWith(named(REMOTE)).toInstance(mock(Context.class));

            bind(PersistenceStrategy.class).toInstance(mock(PersistenceStrategy.class));

        }

    }

    private static final class DummyObject implements Serializable {

        private String uuid = randomUUID().toString();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DummyObject)) return false;
            DummyObject that = (DummyObject) o;
            return Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }

    }

}
