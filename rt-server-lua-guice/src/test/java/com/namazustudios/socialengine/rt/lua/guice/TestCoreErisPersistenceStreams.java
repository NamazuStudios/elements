package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

@Guice(modules = ErisPersistenceTestModule.class)
public class TestCoreErisPersistenceStreams {

    private static final Logger logger = LoggerFactory.getLogger(TestCoreErisPersistenceStreams.class);

    private static TestTemporaryFiles testTemporaryFiles = new TestTemporaryFiles();

    private ResourceLoader resourceLoader;

    @AfterSuite
    public static void deleteTempFiles() {
        testTemporaryFiles.deleteTempFiles();
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

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var fos = new FileOutputStream(tempFile.toFile());
             final var bos = new BufferedOutputStream(fos);
             final var resource = getResourceLoader().load(moduleName)) {
            resource.setVerbose(true);
            resource.serialize(bos);
        }

        try (final var fis = new FileInputStream(tempFile.toFile());
             final var bis = new BufferedInputStream(fis);
             final var resource = getResourceLoader().load(bis)) {
            logger.info("Successfully loaded {}", resource);
        }

    }

    @Test
    public void testIocIsRestoredAfterUnpersist() throws IOException {

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var fos = new FileOutputStream(tempFile.toFile());
             final var bos = new BufferedOutputStream(fos);
             final var resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(bos);

        }

        try (final var fis = new FileInputStream(tempFile.toFile());
             final var bis = new BufferedInputStream(fis);
             final var resource = getResourceLoader().load(bis, true)) {

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

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var fos = new FileOutputStream(tempFile.toFile());
             final var bos = new BufferedOutputStream(fos);
             final var resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(bos);

        }

        try (final var fis = new FileInputStream(tempFile.toFile());
             final var bis = new BufferedInputStream(fis);
             final var resource = getResourceLoader().load(bis, true)) {

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

        final var original = new DummyObject();
        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var fos = new FileOutputStream(tempFile.toFile());
             final var bos = new BufferedOutputStream(fos);
             final var resource = getResourceLoader().load("test.persist_upvalue")) {

            resource.getMethodDispatcher("set_upval")
                    .params(original)
                    .dispatch(o -> assertNull(o), ex -> fail("No error expected."));

            resource.getTasks();
            resource.serialize(bos);

        }

        var toRead = tempFile;
        var toWrite = testTemporaryFiles.createTempFile();

        for (int i = 0; i < 100; ++i) try (final var fis = new FileInputStream(toRead.toFile());
                                           final var bis = new BufferedInputStream(fis);
                                           final var resource = getResourceLoader().load(bis);
                                           final var fos = new FileOutputStream(toWrite.toFile());
                                           final var bos = new BufferedOutputStream(fos)) {

            resource.getMethodDispatcher("assert_upval")
                    .params()
                    .dispatch(o -> assertEquals(original, o), ex -> fail("No error expected."));

            resource.serialize(bos);

            toRead = toWrite;
            toWrite = testTemporaryFiles.createTempFile();

        }

    }

    @Test
    public void testCoroutinesAreRestored() throws IOException {

        final TaskId taskId;
        final Set<TaskId> taskIdSet;

        final var tempFile = testTemporaryFiles.createTempFile();

        try (final var fos = new FileOutputStream(tempFile.toFile());
             final var bos = new BufferedOutputStream(fos);
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
            resource.serialize(bos);

        }

        try (final var fis = new FileInputStream(tempFile.toFile());
             final var bis = new BufferedInputStream(fis);
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
    public void setResourceLoader(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
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
