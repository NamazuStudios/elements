package com.namazustudios.socialengine.rt.util;

import com.namazustudios.socialengine.rt.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.namazustudios.socialengine.rt.Constants.*;
import static java.lang.String.format;
import static java.nio.file.Files.*;

/**
 * Manages temporary files.
 */
public class TemporaryFiles {

    private static final Logger logger;

    private static final Path TEMPORARY_ROOT;

    private static final boolean AUTO_PURGE_TEMP;

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(TemporaryFiles.class);

    private static final FileVisitor<Path> DELETE_RECURSIVE = new SimpleFileVisitor<>() {

        @Override
        public FileVisitResult visitFile(final Path file,
                                         final BasicFileAttributes attrs) {

            try {
                deleteIfExists(file);
            } catch (IOException ex) {
                logger.warn("Could not delete temp file: {}", file, ex);
            }

            return FileVisitResult.CONTINUE;
        }


        @Override
        public FileVisitResult postVisitDirectory(final Path dir,
                                                  final IOException exc) throws IOException {
            deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
        }

    };

    static {

        logger = LoggerFactory.getLogger(TemporaryFiles.class);

        final var tempDirectory = System.getenv(ELEMENTS_TEMP);

        final Path tempDirectoryPath;

        if (tempDirectory == null) {
            final var homeDir = System.getenv().getOrDefault(Constants.ELEMENTS_HOME, ".");
            tempDirectoryPath = Paths.get(homeDir, ELEMENTS_TEMP_DEFAULT);
        } else {
            tempDirectoryPath = Paths.get(tempDirectory);
        }

        final var systemTempDirectory = System.getProperty("java.io.tmpdir");

        var root = Paths.get(systemTempDirectory);

        try {
            createDirectories(tempDirectoryPath);
            root = tempDirectoryPath;
        } catch (IOException ex) {
            logger.warn("Could not create temporary directory {}. Using system default at {}",
                tempDirectoryPath,
                root,
                ex);
        }

        final var purge = System.getenv().getOrDefault(ELEMENTS_TEMP_PURGE, ELEMENTS_TEMP_PURGE_DEFAULT);

        TEMPORARY_ROOT = root;
        AUTO_PURGE_TEMP = Boolean.parseBoolean(purge);

    }

    private final String prefix;

    private final Deque<Path> toPurge = new ConcurrentLinkedDeque<>();

    public TemporaryFiles(final Class<?> enclosingClass) {
        this(enclosingClass.getName());
    }

    public TemporaryFiles(final String prefix) {
        this.prefix = prefix;
        shutdownHooks.add(this::deleteTempFilesAndDirectories);
    }

    public void deleteTempFilesAndDirectories() {

        if (AUTO_PURGE_TEMP) {
            toPurge.forEach(p -> {
                try {
                    walkFileTree(p, DELETE_RECURSIVE);
                } catch (FileNotFoundException | NoSuchFileException ex) {
                    logger.trace("Could not delete temp file: {}", p, ex);
                } catch (IOException ex) {
                    logger.warn("Could not delete temp file: {}", p, ex);
                }
            });
        }

        toPurge.clear();

    }

    public Path createTempDirectory() throws UncheckedIOException {
        return createTempDirectory(prefix);
    }

    public Path createTempDirectory(final String prefix) throws UncheckedIOException {

        final Path path;

        try {
            path = Files.createTempDirectory(TEMPORARY_ROOT, prefix);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        final var absolute = path.toAbsolutePath();
        toPurge.add(absolute);

        return absolute;

    }

    public Path createTempFile() throws UncheckedIOException {
        return createTempFile(null);
    }

    public Path createTempFile(final String suffix) throws UncheckedIOException {
        return createTempFile(null, suffix);
    }

    public Path createTempFile(final String prefix, final String suffix) throws UncheckedIOException {

        final Path path;

        try {
            final var fullPrefix = format("%s%s", this.prefix, prefix == null ? "" : prefix);
            path = Files.createTempFile(TEMPORARY_ROOT, fullPrefix, suffix);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        final var absolute = path.toAbsolutePath();
        toPurge.add(absolute);

        return absolute;

    }

}
