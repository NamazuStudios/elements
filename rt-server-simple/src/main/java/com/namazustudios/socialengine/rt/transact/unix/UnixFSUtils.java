package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.namazustudios.socialengine.rt.transact.Revision.infinity;
import static com.namazustudios.socialengine.rt.transact.Revision.zero;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionDataStore.STORAGE_ROOT_DIRECTORY;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.isDirectory;
import static java.util.Comparator.naturalOrder;

/**
 * A collection of useful utility routines when accessing the filesystem.
 */
public class UnixFSUtils {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSUtils.class);

    public static final String LOCK_FILE_NAME = "lock";

    public static final String PATHS_DIRECTORY = "paths";

    public static final String RESOURCES_DIRECTORY = "resources";

    private final RevisionFactory revisionFactory;

    private final Path pathStorageRoot;

    private final Path resourceStorageRoot;

    @Inject
    public UnixFSUtils(
            final RevisionFactory revisionFactory,
            @Named(STORAGE_ROOT_DIRECTORY) final Path storageRoot) {
        this.revisionFactory = revisionFactory;
        this.pathStorageRoot = storageRoot.resolve(PATHS_DIRECTORY).toAbsolutePath();
        this.resourceStorageRoot = storageRoot.resolve(RESOURCES_DIRECTORY).toAbsolutePath();
    }

    /**
     * Locks a directory by writing a lock file.  If the directory is already locked, then this will throw an instance
     * of {@link FileAlreadyExistsException} indicating that another process has locked the directory.
     *
     * @param directoryPath the path to the directory
     * @return the {@link Path} to the lock file
     * @throws IOException if a locking error occurs,
     */
    public Path lockDirectory(final Path directoryPath) throws IOException {

        final Path path = Paths.get(".", LOCK_FILE_NAME);
        final Path lockFilePath = directoryPath.toAbsolutePath().resolve(path).toAbsolutePath();

        if (Files.exists(lockFilePath)) {
            final String msg = format("Journal path is locked %s", lockFilePath);
            throw new FileAlreadyExistsException(msg);
        } else {
            Files.createFile(lockFilePath);
        }

        return lockFilePath;

    }

    /**
     * Unlocks the directory by deleting the specified lock file as the supplied {@link Path}
     * @param lockFilePath the lock file path
     */
    public void unlockDirectory(final Path lockFilePath) {
        try {
            deleteIfExists(lockFilePath);
        } catch (IOException e) {
            logger.error("Failed to delete lock file.", e);
        }
    }



    /**
     * Searches the supplied {@link Path} to a directory and finds the most suitable revision.  This is the file with
     * highest {@link Revision<Path>} that is the same as or less than the {@link Revision<?>}.  If no such revision
     * exists, then this will return the value of {@link Revision#infinity()} indicating that no such value exists.
     *
     * @param directory the {@link Path} to a directory holding revisioned content.
     * @param revision the {@link Revision<?>} to use as reference
     * @return the {@link Revision<Path>} pointing to the requested file.
     */
    public Revision<Path> getFileForRevision(final Path directory, final Revision<?> revision) {

        if (!isDirectory(directory)) throw new IllegalArgumentException(directory + " must be a directory.");

        return doOperation(() -> Files
            .list(directory)
            .filter(Files::isRegularFile)
            .map(file -> revisionFactory.create(file.getFileName().toString(), file))
            .filter(r -> r.isBeforeOrSame(revision))
            .max(naturalOrder())
            .orElse(zero()));

    }

    /**
     * Performs and operation that may throw an instance of {@link IOException}, and re-throws it wrapped inside of a
     * {@link InternalException}.
     *
     * @param action the action to perform
     * @param <T> the return type
     * @return the value returned from the {@link IOOperation<T>}
     */
    public <T> T doOperation(final IOOperation<T> action) {
        try {
            return action.perform();
        } catch (Exception ex) {
            logger.error("IOException Performing operation.", ex);
            throw new InternalException(ex);
        }
    }

    /**
     * Returns tha {@link Path} to the directory holding the path mapping.
     * @return the {@link Path} to the path mapping
     */
    public Path getPathStorageRoot() {
        return pathStorageRoot;
    }

    /**
     * Returns the storage root directory where {@link Resource} instances are stored.
     *
     * @return the {@link Path} to the {@link Resource} storage
     */
    public Path getResourceStorageRoot() {
        return resourceStorageRoot;
    }

    /**
     * Defines an operation which may throw an instance of {@link IOException}
     * @param <T> the result type
     */
    @FunctionalInterface
    public interface IOOperation<T> {

        /**
         * Performs the operation.
         *
         * @return the calculated vallue of the operation.
         * @throws IOException for any reason.
         */
        T perform() throws IOException;

    }

}
