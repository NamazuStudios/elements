package com.namazustudios.socialengine.rt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.nio.file.Files.deleteIfExists;

/**
 *
 */
public class TestTemporaryFiles {

    private static final Logger logger = LoggerFactory.getLogger(TestTemporaryFiles.class);

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(TestTemporaryFiles.class);

    private static final TestTemporaryFiles defaultInstance = new TestTemporaryFiles(TestTemporaryFiles.class);

    private Deque<Path> toPurge = new ConcurrentLinkedDeque<>();

    private final Class<?> testCaseClass;

    public static TestTemporaryFiles getDefaultInstance() {
        return defaultInstance;
    }

    public TestTemporaryFiles(final Class<?> testCaseClass) {
        this.testCaseClass = testCaseClass;
        shutdownHooks.add(this::deleteTempFiles);
    }

    public void deleteTempFiles() {
        toPurge.forEach(p -> {
            try {
                deleteIfExists(p);
            } catch (IOException e) {
                logger.warn("Could not delete temp file.", e);
            }
        });
        toPurge.clear();
    }

    public Path createTempDirectory(final String prefix) throws IOException {
        final var path = Files.createTempDirectory(testCaseClass.getSimpleName());
        toPurge.add(path);
        return path;
    }

    public Path createTempFile() throws IOException {
        final var path = Files.createTempFile(testCaseClass.getSimpleName(), "resource");
        toPurge.add(path);
        return path;
    }

}
