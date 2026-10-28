package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.record.ElementStaticContentRecord;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Regression coverage for #178: the static file index included <em>directory</em> entries, so a request
 * for a directory path (e.g. {@code /schedule}, an SPA deep link for {@code schedule/index.html})
 * resolved to the directory's metadata and serving it failed with
 * {@code FileSystemException: ... is a directory} (a 500).
 * <p>
 * The index must only contain files; a directory containing an index file resolves to that file,
 * mirroring the root-index behavior.
 */
public class StaticRuleEngineTest {

    @Test
    public void directoryEntriesResolveToTheirIndexFile() throws IOException {

        final var root = Files.createTempDirectory("static-rule-engine-test");

        Files.writeString(root.resolve("index.html"), "root index");
        Files.createDirectories(root.resolve("schedule"));
        Files.writeString(root.resolve("schedule").resolve("index.html"), "schedule index");
        Files.writeString(root.resolve("schedule").resolve("page.html"), "schedule page");
        Files.createDirectories(root.resolve("empty")); // no index file inside

        // Mirror DirectoryElementPathLoader#collectDirEntriesRecursive, which includes directory entries.
        final List<Path> contents = new ArrayList<>();
        try (var walk = Files.walk(root)) {
            walk.filter(p -> !p.equals(root)).forEach(contents::add);
        }

        final var contentRecord = new ElementStaticContentRecord(root, List.copyOf(contents));

        final var logs = new ArrayList<String>();
        final var warnings = new ArrayList<String>();
        final var errors = new ArrayList<Throwable>();

        final var pending = new Loader.PendingDeployment(
                uri -> true,
                logs::add,
                warnings::add,
                errors::add,
                element -> {}
        );

        final var attributes = new SimpleAttributes.Builder().build();

        final var config = new StaticRuleEngine(
                contentRecord, attributes, "testns", pending, "max-age=0").buildIndex();

        // No index entry may point at a directory: serving one is the 500 from the report.
        for (final var entry : config.files().entrySet()) {
            assertTrue(
                    !Files.isDirectory(entry.getValue().absolutePath()),
                    "index entry '" + entry.getKey() + "' must not be a directory"
            );
        }

        // A directory containing an index file must resolve to that file, with and without trailing slash.
        final var scheduleIndex = config.files().get("schedule/index.html");
        assertNotNull(scheduleIndex, "schedule/index.html must be indexed");

        assertEquals(config.files().get("schedule"), scheduleIndex,
                "a directory request must resolve to the directory's index file");
        assertEquals(config.files().get("schedule/"), scheduleIndex,
                "a trailing-slash directory request must resolve to the directory's index file");

        // The root index behavior is unchanged.
        assertEquals(config.files().get("index.html"), config.indexFile());

        // A directory with no index file must simply be absent (404 on request), never a directory entry.
        assertTrue(!config.files().containsKey("empty") && !config.files().containsKey("empty/"),
                "a directory without an index file must not be indexed");

        assertTrue(errors.isEmpty(), "no errors expected while building the index: " + errors);

    }

}
