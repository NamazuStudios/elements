package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;
import static com.namazustudios.socialengine.rt.Context.REMOTE;
import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

@Guice(modules = TestCoreErisPersistence.Module.class)
public class TestCoreErisPersistence {

    private static final Logger logger = LoggerFactory.getLogger(TestCoreErisPersistence.class);

    private ResourceLoader resourceLoader;

    @DataProvider
    public static Object[][] allLuaResources() {

        // This ensures that we can persist all Lua source code provided in this package, including test code.

        final Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forJavaClassPath())
            .setScanners(new ResourcesScanner()));

        final Set<String> luaResources = new TreeSet<>(reflections.getResources(Pattern.compile(".*\\.lua")));

        return luaResources
            .stream()
            .map(s -> s.replace('/', '.').substring(0, s.length() - ".lua".length()))
            .filter(s -> !"main".equals(s))              // Manifests aren't persistence aware
            .map(s -> new Object[]{s})
            .toArray(Object[][]::new);

    }

    @Test(dataProvider = "allLuaResources", invocationCount = 10)
    public void testPersistUnpersist(final String moduleName) throws IOException {

        logger.info("Testing Persistence for {}", moduleName);

        final var tempFile = createTempFile(TestCoreErisPersistence.class.getSimpleName(), "resource");

        try (final FileChannel wbc = open(tempFile, WRITE);
             final Resource resource = getResourceLoader().load(moduleName)) {
            resource.setVerbose(true);
            resource.serialize(wbc);
        }

        try (final FileChannel rbc = open(tempFile, READ);
             final Resource resource = getResourceLoader().load(rbc)) {
            logger.info("Successfully loaded {}", resource);
        }

    }

    @Test
    public void testIocIsRestoredAfterUnpersist() throws IOException {

        final var tempFile = createTempFile(TestCoreErisPersistence.class.getSimpleName(), "resource");

        try (final FileChannel wbc = open(tempFile, WRITE);
             final Resource resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(wbc);

        }

        try (final FileChannel rbc = open(tempFile, READ);
             final Resource resource = getResourceLoader().load(rbc, true)) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

        }

    }

    @Test
    public void testIocProviderIsRestoredAfterUnpersist() throws IOException {

        final var tempFile = createTempFile(TestCoreErisPersistence.class.getSimpleName(), "resource");

        try (final FileChannel wbc = open(tempFile, WRITE);
             final Resource resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(wbc);

        }

        try (final FileChannel rbc = open(tempFile, READ);
             final Resource resource = getResourceLoader().load(rbc, true)) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

        }

    }

    @Test(threadPoolSize = 100, invocationCount = 100)
    public void testUpvaluesArePersisted() throws Exception {

        final Set<TaskId> tasks;
        final DummyObject original = new DummyObject();
        final var tempFile = createTempFile(TestCoreErisPersistence.class.getSimpleName(), "resource");

        try (final FileChannel wbc = open(tempFile, WRITE);
             final Resource resource = getResourceLoader().load("test.persist_upvalue")) {

            resource.getMethodDispatcher("set_upval")
                    .params(original)
                    .dispatch(o -> assertNull(o), ex -> fail("No error expected."));

            tasks = resource.getTasks();
            resource.serialize(wbc);

        }

        for (int i = 0; i < 100; ++i) try (final FileChannel rbc = open(tempFile, READ);
                                           final Resource resource = getResourceLoader().load(rbc)) {

            resource.getMethodDispatcher("assert_upval")
                    .params()
                    .dispatch(o -> assertEquals(original, o), ex -> fail("No error expected."));

            resource.serialize(rbc);

        }

    }

    @Test
    public void testCoroutinesAreRestored() throws IOException {

        final TaskId taskId;
        final Set<TaskId> taskIdSet;

        final var tempFile = createTempFile(TestCoreErisPersistence.class.getSimpleName(), "resource");

        try (final FileChannel wbc = open(tempFile, WRITE);
             final Resource resource = getResourceLoader().load("test.simple_yield")) {

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

        try (final FileChannel rbc = open(tempFile, READ);
             final Resource resource = getResourceLoader().load(rbc, true)) {
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

            // Configurations that are ne
//            bind(Integer.class).annotatedWith(named(SCHEDULER_THREADS)).toInstance(1);
//            bind(Long.class).annotatedWith(named(HANDLER_TIMEOUT_MSEC)).toInstance(90l);

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
