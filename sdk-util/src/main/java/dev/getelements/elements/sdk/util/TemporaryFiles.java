package dev.getelements.elements.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.lang.String.format;
import static java.nio.file.FileVisitResult.CONTINUE;
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
 * This class is thread safe and is suitable for use in static memory space. It is recommended to scope a single
 * instance of TemporaryFiles to a type for convenient organization and access.
 *
 * {@see {@link Environment#ELEMENTS_TEMP}}
 * {@see {@link Environment#ELEMENTS_TEMP_DEFAULT}}
 * {@see {@link Environment#ELEMENTS_TEMP_PURGE}}
 * {@see {@link Environment#ELEMENTS_TEMP_PURGE_DEFAULT}}
 *
 */
public class TemporaryFiles {

    private static final Logger logger;

    private static final Path TEMPORARY_ROOT;

    private static final boolean AUTO_PURGE_TEMP;

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(TemporaryFiles.class);

    static {

        logger = LoggerFactory.getLogger(TemporaryFiles.class);

        final var tempDirectory = System.getenv(Environment.ELEMENTS_TEMP);

        final Path tempDirectoryPath;

        if (tempDirectory == null) {
            final var homeDir = System.getenv().getOrDefault(Environment.ELEMENTS_HOME, ".");
            tempDirectoryPath = Paths.get(homeDir, Environment.ELEMENTS_TEMP_DEFAULT);
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

        final var purge = System.getenv().getOrDefault(Environment.ELEMENTS_TEMP_PURGE, Environment.ELEMENTS_TEMP_PURGE_DEFAULT);

        TEMPORARY_ROOT = root;
        AUTO_PURGE_TEMP = Boolean.parseBoolean(purge);

    }

    /**
     * Returns the temporary path root directory.
     *
     * @return the temporary path root
     */
    public static Path getTemporaryRoot() {
        return TEMPORARY_ROOT;
    }

    /**
     * Tests if a supplied {@link Path} is a temporary path under the structure of this type.
     * @param path the path
     *
     * @return true if it is under the temporary root, false otherwise
     */
    public static boolean isTemporaryPath(final Path path) {
        return path.startsWith(getTemporaryRoot());
    }

    private final String prefix;

    private final ThreadSafeLazyValue<Path> root;

    /**
     * Creates an instance of {@link TemporaryFiles} with the enclosing class. This simply uses the supplied class'
     * fully qualifie dname as the prefix such that the temporary files may be identified later.
     *
     * @param enclosingClass the enclosing class
     */
    public TemporaryFiles(final Class<?> enclosingClass) {
        this(enclosingClass.getName());
    }

    /**
     * Creates an instance of {@link TemporaryFiles} with the supplied prefix. The prefix is used in all subsequent
     * operations to create and manage temporary files.
     *
     * @param prefix the prefix to use
     */
    public TemporaryFiles(final String prefix) {

        this.root = new ThreadSafeLazyValue<>(() -> {
            try {
                return Files.createTempDirectory(TEMPORARY_ROOT, prefix).toAbsolutePath().normalize();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        this.prefix = prefix;
        shutdownHooks.add(this::deleteTempFilesAndDirectories);
        shutdownHooks.add(() -> { if (AUTO_PURGE_TEMP) deleteIfExists(root.get()); });

    }

    /**
     * Deletes all temporary files and directories created by this {@link TemporaryFiles} instance.
     */
    public void deleteTempFilesAndDirectories() {
        if (AUTO_PURGE_TEMP) {
            try {
                walkFileTree(root.get(), new DeleteRecursive());
            } catch (FileNotFoundException | NoSuchFileException ex) {
                logger.trace("Could not delete temp file: {}", root, ex);
            } catch (IOException ex) {
                logger.warn("Could not delete temp file: {}", root, ex);
            }
        }
    }

    /**
     * Creates a temporary directory using this instance's prefix.
     *
     * @return the {@link Path} to the directory
     * @throws UncheckedIOException if there is a problem creating the directory.
     *
     */
    public Path createTempDirectory() throws UncheckedIOException {
        return createTempDirectory(null);
    }

    /**
     * Creates a temporary directory, using a specific prefix. The prefix supplied will be appended to this instance's
     * prefix.
     *
     * @return the {@link Path} to the directory
     * @throws UncheckedIOException if there is a problem creating the directory.
     *
     */
    public Path createTempDirectory(final String prefix) throws UncheckedIOException {

        final Path path;

        try {
            path = Files.createTempDirectory(root.get(), prefix);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return path.toAbsolutePath().normalize();

    }

    /**
     * Creates a temporary file.
     *
     * @return the temporary file {@link Path}
     *
     * @throws UncheckedIOException if an error occurred creating the temporary file.
     */
    public Path createTempFile() throws UncheckedIOException {
        return createTempFile(null);
    }

    /**
     * Creates a temporary file with the supplied suffix and the default prefix.
     *
     * @param suffix the suffix the suffix of the file.
     * @return the temporary file {@link Path}
     * @throws UncheckedIOException if an error occurred creating the temporary file.
     */
    public Path createTempFile(final String suffix) throws UncheckedIOException {
        return createTempFile(null, suffix);
    }

    /**
     * Creates a temporary file with the supplied prefix and suffix.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the temporary file {@link Path}
     *
     * @throws UncheckedIOException if an error occurred creating the temporary file.
     */
    public Path createTempFile(final String prefix, final String suffix) throws UncheckedIOException {

        final Path path;

        try {
            path = Files.createTempFile(root.get(), prefix, suffix);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return path.toAbsolutePath().normalize();

    }

    /**
     * Creates a temporary file with the supplied prefix and suffix.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     * @return the temporary file {@link Path}
     *
     * @throws UncheckedIOException if an error occurred creating the temporary file.
     */
    public Path createTempFile(final String prefix, final String suffix, final Path parent) throws UncheckedIOException {

        final Path path;

        try {
            final var fullPrefix = format("%s%s", this.prefix, prefix == null ? "" : prefix);
            path = Files.createTempFile(parent, fullPrefix, suffix);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return path.toAbsolutePath().normalize();

    }

    private class DeleteRecursive extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(final Path file,
                                         final BasicFileAttributes attrs) {
            return safeDelete(file);
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
            return safeDelete(dir);
        }

        private FileVisitResult safeDelete(final Path path) {

            try {
                if (!root.get().equals(path)) deleteIfExists(path);
            } catch (FileNotFoundException | NoSuchFileException ex) {
                logger.trace("Could not delete temp file: {}", path, ex);
            }catch (IOException ex) {
                logger.warn("Could not delete temp file: {}", path, ex);
            }

            return CONTINUE;
        }

    }

}
