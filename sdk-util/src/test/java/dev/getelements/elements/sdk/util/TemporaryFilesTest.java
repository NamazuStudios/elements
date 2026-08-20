package dev.getelements.elements.sdk.util;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers {@link TemporaryFiles#deleteRecursively(Path)} and the stale-directory purge that runs when a new
 * instance is constructed. Assertions are limited to on-disk path existence (deterministic) rather than any
 * OS-level resource accounting (file descriptor counts, RSS, etc.), which is inherently flaky across
 * platforms and dependent on GC/finalizer timing.
 */
public class TemporaryFilesTest {

    @Test
    public void deleteRecursivelyRemovesFilesAndNestedDirectories() throws IOException {

        final var root = Files.createTempDirectory("delete-recursively-test-");
        final var nestedDir = Files.createDirectories(root.resolve("nested"));
        final var rootFile = Files.createFile(root.resolve("file.txt"));
        final var nestedFile = Files.createFile(nestedDir.resolve("nested-file.txt"));

        assertTrue(Files.exists(rootFile));
        assertTrue(Files.exists(nestedFile));

        TemporaryFiles.deleteRecursively(root);

        assertFalse(Files.exists(root), "Directory should be removed");
        assertFalse(Files.exists(nestedDir), "Nested directory should be removed");
        assertFalse(Files.exists(rootFile), "File should be removed");
        assertFalse(Files.exists(nestedFile), "Nested file should be removed");

    }

    @Test
    public void deleteRecursivelyToleratesMissingPath() {
        final var missing = Path.of(System.getProperty("java.io.tmpdir"), "does-not-exist-" + getClass().getName());
        assertFalse(Files.exists(missing));
        // Must not throw even though nothing exists at this path.
        TemporaryFiles.deleteRecursively(missing);
    }

    @Test
    public void newInstancePurgesStaleDirectoriesLeftByPriorInstanceWithSamePrefix() throws IOException {

        final var prefix = "TemporaryFilesPurgeTest";

        final var first = new TemporaryFiles(prefix);
        final var firstDir = first.createTempDirectory();
        final var firstInstanceRoot = firstDir.getParent();

        assertTrue(Files.isDirectory(firstInstanceRoot));
        assertTrue(firstInstanceRoot.getFileName().toString().startsWith(prefix));
        assertTrue(Files.exists(firstDir));

        // Constructing a second instance with the same prefix simulates a new process starting up after
        // the prior one (represented by `first`) crashed without running its shutdown hook. The stale
        // directory tree left behind by `first` should be purged before `second` creates its own.
        final var second = new TemporaryFiles(prefix);

        assertFalse(Files.exists(firstInstanceRoot),
                "Stale directory from prior instance with the same prefix should be purged");
        assertFalse(Files.exists(firstDir));

        // The new instance must still be fully usable after the purge.
        final var secondDir = second.createTempDirectory();
        assertTrue(Files.exists(secondDir));

    }

}
