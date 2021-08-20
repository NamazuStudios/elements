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
 * Manages temporary files. Each instance of {@link TemporaryFiles} makes a temporary directory which houses all
 * temporary files. The directory will be automatically deleted when requested or when the VM shuts down (assuming a
 * clean shutdown).
 *
 * This {@link TemporaryFiles} type controls disk location using a set of environment variables. If, for some reason,
 * this instance cannot use the configured path this will log a warning and defer to the value of the "java.io.tmpdir"
 * system property.
 *
 * {@see {@link Constants#ELEMENTS_TEMP}}
 * {@see {@link Constants#ELEMENTS_TEMP_DEFAULT}}
 * {@see {@link Constants#ELEMENTS_TEMP_PURGE}}
 * {@see {@link Constants#ELEMENTS_TEMP_PURGE_DEFAULT}}
 *
 */
public class TemporaryFiles {

    private static final Logger logger;

    private static final Path TEMPORARY_ROOT;

    private static final boolean AUTO_PURGE_TEMP;

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(TemporaryFiles.class);

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

    private final Path root;

    private final String prefix;

    /**
     * Creates an instance of {@link TemporaryFiles} with the enclosing class.
     * @param enclosingClass
     */
    public TemporaryFiles(final Class<?> enclosingClass) {
        this(enclosingClass.getName());
    }

    public TemporaryFiles(final String prefix) {

        try {
            this.root = Files.createTempDirectory(TEMPORARY_ROOT, prefix).toAbsolutePath().normalize();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        this.prefix = prefix;
        shutdownHooks.add(this::deleteTempFilesAndDirectories);
        shutdownHooks.add(() -> {
            if (AUTO_PURGE_TEMP) deleteIfExists(root);
        });

    }

    public void deleteTempFilesAndDirectories() {
        if (AUTO_PURGE_TEMP) {
            try {
                walkFileTree(root, new DeleteRecursive());
            } catch (FileNotFoundException | NoSuchFileException ex) {
                logger.trace("Could not delete temp file: {}", root, ex);
            } catch (IOException ex) {
                logger.warn("Could not delete temp file: {}", root, ex);
            }
        }
    }

    public Path createTempDirectory() throws UncheckedIOException {
        return createTempDirectory(prefix);
    }

    public Path createTempDirectory(final String prefix) throws UncheckedIOException {

        final Path path;

        try {
            path = Files.createTempDirectory(root, prefix);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return path.toAbsolutePath().normalize();

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
            path = Files.createTempFile(root, fullPrefix, suffix);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return path.toAbsolutePath().normalize();

    }

    private class DeleteRecursive extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(final Path file,
                                         final BasicFileAttributes attrs) {

            try {
                if (!root.equals(file)) deleteIfExists(file);
            } catch (IOException ex) {
                logger.warn("Could not delete temp file: {}", file, ex);
            }

            return FileVisitResult.CONTINUE;
        }


        @Override
        public FileVisitResult postVisitDirectory(final Path dir,
                                                  final IOException exc) throws IOException {
            if (!root.equals(dir)) deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
        }

    }


}
