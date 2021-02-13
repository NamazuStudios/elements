package com.namazustudios.socialengine.rt.lua.guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.nio.file.Files.deleteIfExists;

public class TestTemporaryFiles {

    private static final Logger logger = LoggerFactory.getLogger(TestTemporaryFiles.class);

    private Deque<Path> toPurge = new ConcurrentLinkedDeque<>();

    public void deleteTempFiles() {
        toPurge.forEach(p -> {
            try {
                deleteIfExists(p);
            } catch (IOException e) {
                logger.warn("Could not delete temp file.", e);
            }
        });
    }

    public Path createTempFile() throws IOException {
        return Files.createTempFile(TestCoreErisPersistenceStreams.class.getSimpleName(), "resource");
    }

}
