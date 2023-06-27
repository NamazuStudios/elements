package dev.getelements.elements.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dev.getelements.elements.rt.transact.Revision;
import dev.getelements.elements.rt.util.ShutdownHooks;
import dev.getelements.elements.rt.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.google.common.collect.Streams.concat;
import static com.google.inject.name.Names.named;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

@Guice(modules = {UnixFSUtilsTest.Module.class})
public class UnixFSUtilsTest {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(UnixFSUtilsTest.class);

    private static final ShutdownHooks hooks = new ShutdownHooks(UnixFSUtilsTest.class);

    private static final Logger logger = LoggerFactory.getLogger(UnixFSUtilsTest.class);

    @Inject
    private UnixFSUtils utils;

    private Deque<Path> garbage = new ConcurrentLinkedDeque<Path>();

    @BeforeClass
    public void setup() {
        hooks.add(this::cleanup);
    }

    @Test
    public void testList() throws Exception {

        final var tmp = temporaryFiles.createTempDirectory("unixfs-utils-test-list");
        garbage.add(tmp);

        populate(tmp);

        final var pathList = utils.list(tmp).collect(toList());
        assertEquals(pathList.size(), 12);

        final var actual = new HashSet<>(pathList);
        assertEquals(actual.size(), pathList.size());

        final var expected = concat(
            range(0, 10).mapToObj(i -> tmp.resolve(format("file-%d", i))),
            range(0, 2).mapToObj(i -> tmp.resolve(format("subdir-%d", i)))
        ).collect(toSet());

        assertEquals(actual, expected);

    }

    @Test
    public void testListCompareJavaNioFilesList() throws Exception {

        final var tmp = temporaryFiles.createTempDirectory("unixfs-utils-test-list-java-nio-files-list");
        garbage.add(tmp);

        populate(tmp);

        final var expected = Files.list(tmp).collect(toSet());

        final var pathList = utils.list(tmp).collect(toList());
        assertEquals(pathList.size(), expected.size());

        final var actual = new HashSet<>(pathList);
        assertEquals(actual.size(), pathList.size());

        assertEquals(actual, expected);

    }

    @Test
    public void testWalk() throws Exception {

        final var tmp = temporaryFiles.createTempDirectory("unixfs-utils-test-walk");
        garbage.add(tmp);

        final var expected = populate(tmp);

        final var pathList = utils.walk(tmp).collect(toList());
        assertEquals(pathList.size(), 181);

        final var actual = new HashSet<>(pathList);
        assertEquals(actual.size(), pathList.size());

        assertEquals(actual, expected);

    }

    @Test
    public void testListCompareJavaNioFilesWalk() throws Exception {

        final var tmp = temporaryFiles.createTempDirectory("unixfs-utils-test-list-java-nio-files-walk");
        garbage.add(tmp);

        populate(tmp);

        final var expected = Files.walk(tmp).collect(toSet());

        final var pathList = utils.walk(tmp).collect(toList());
        assertEquals(pathList.size(), expected.size());

        final var actual = new HashSet<>(pathList);
        assertEquals(actual.size(), pathList.size());

        assertEquals(actual, expected);

    }

    private Set<Path> populate(final Path directory) throws IOException {
        return populate(directory, 4, new HashSet<>());
    }

    private Set<Path> populate(final Path directory, final int depth, final Set<Path> result) throws IOException {

        result.add(directory);

        if (depth <= 0) {
            return result;
        }

        for (int i = 0; i < 10; ++i) {
            final var file = directory.resolve(format("file-%d", i));
            createFile(file);
            result.add(file);
        }

        for (int i = 0; i < 2; ++i) {
            final var subdir = directory.resolve(format("subdir-%d", i));
            createDirectory(subdir);
            populate(subdir, depth - 1, result);
        }

        return result;

    }

    @AfterClass
    public void cleanup() throws IOException {
        for (var path : garbage) {

            if (!exists(path)) continue;

            Files.walk(path)
                 .sorted(Comparator.reverseOrder())
                 .forEach(file -> {
                     try {
                         deleteIfExists(file);
                     } catch (IOException ex) {
                         // Should not happen and it shouldn't break the tests, but definitely want to know if
                         // if does happen.
                         logger.error("Caught exception deleting temp file. Ignoring.", ex);
                     }
                 });
        }
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            final var tmp = temporaryFiles.createTempDirectory("unixfs-utils-test");
            bind(Path.class).annotatedWith(named(UnixFSUtils.UNIXFS_STORAGE_ROOT_DIRECTORY)).toInstance(tmp);
            bind(Revision.Factory.class).toInstance(mock(Revision.Factory.class));
        }

    }

    @FunctionalInterface
    public interface Populator { public void populate(Path path, PopulationType type) throws IOException; }

    public enum PopulationType { FILE, DIRECTORY }

}
