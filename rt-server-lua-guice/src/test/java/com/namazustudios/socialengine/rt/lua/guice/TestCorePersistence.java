package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.ws.rs.client.Client;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.HandlerContext.HANDLER_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

@Guice(modules = TestCorePersistence.Module.class)
public class TestCorePersistence {

    private static final Logger logger = LoggerFactory.getLogger(TestCorePersistence.class);

    private ResourceLoader resourceLoader;

    private Context context;

    @BeforeClass
    private void start() {
        getContext().start();
    }

    @AfterClass
    private void stop() {
        getContext().shutdown();
    }

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

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final Resource resource = getResourceLoader().load(moduleName)) {
            resource.setVerbose(true);
            resource.serialize(bos);
            bytes = bos.toByteArray();
        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final Resource resource = getResourceLoader().load(bis)) {
            logger.info("Successfully loaded {}", resource);
        }

    }

    @Test
    public void testIocIsRestoredAfterUnpersist() throws IOException {

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final Resource resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(bos);
            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final Resource resource = getResourceLoader().load(bis, true)) {

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

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final Resource resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(bos);
            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final Resource resource = getResourceLoader().load(bis, true)) {

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

        byte[] bytes;
        final Set<TaskId> tasks;
        final DummyObject original = new DummyObject();

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final Resource resource = getResourceLoader().load("test.persist_upvalue")) {

            resource.getMethodDispatcher("set_upval")
                    .params(original)
                    .dispatch(o -> assertNull(o), ex -> fail("No error expected."));

            tasks = resource.getTasks();
            resource.serialize(bos);
            bytes = bos.toByteArray();

        }

        for (int i = 0; i < 100; ++i) try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                           final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                                           final Resource resource = getResourceLoader().load(bis)) {

            resource.getMethodDispatcher("assert_upval")
                    .params()
                    .dispatch(o -> assertEquals(original, o), ex -> fail("No error expected."));

            resource.serialize(bos);
            bytes = bos.toByteArray();

        }

    }

    @Test
    public void testCoroutinesAreRestored() throws IOException {

        final byte[] bytes;

        final TaskId taskId;
        final Set<TaskId> taskIdSet;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
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
            resource.serialize(bos);
            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final Resource resource = getResourceLoader().load(bis, true)) {
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

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            install(new LuaModule());
            install(new SimpleContextModule());

            bind(Client.class).toInstance(mock(Client.class));
            bind(IocResolver.class).to(GuiceIoCResolver.class).asEagerSingleton();
            bind(AssetLoader.class).to(ClasspathAssetLoader.class).asEagerSingleton();
            bind(Integer.class).annotatedWith(named(SCHEDULER_THREADS)).toInstance(1);
            bind(Long.class).annotatedWith(named(HANDLER_TIMEOUT_MSEC)).toInstance(90l);

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
