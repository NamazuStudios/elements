package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.Revision.zero;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Comparator.naturalOrder;

/**
 * A collection of useful utility routines when accessing the filesystem.
 */
public class UnixFSUtils {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSUtils.class);

    public static final String STORAGE_ROOT_DIRECTORY = "com.namazustudios.socialengine.rt.transact.unix.fs.root";

    public static final String REVISION_SUFFIX = "rlink";

    public static final String REVISION_SYMBOLIC_LINK_SUFFIX = "rsymlink";

    public static final String DIRECTORY_SUFFIX = "d";

    public static final String LOCK_FILE_NAME = "lock";

    public static final String HEAD_FILE_NAME = "head";

    public static final String REVISION_POOL_FILE_NAME = "rpool";

    public static final String TRANSACTION_JOURNAL_FILE_NAME = "journal";

    public static final String PATHS_DIRECTORY = "paths";

    public static final String REVERSE_DIRECTORY = "reverse";

    public static final String RESOURCES_DIRECTORY = "resources";

    public static final String TEMPORARY_DIRECTORY = "temporary";

    public static final String TOMBSTONE_FILE_NAME = "tombstone";

    public static final int TEMP_NAME_LENGTH_CHARS = 128;

    public static final String TEMP_FILE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHI0123456789-";

    private Revision.Factory revisionFactory;

    private final Path storageRoot;

    private final Path revisionTablePath;

    private final Path revisionPoolPath;

    private final Path transactionJournalPath;

    private final Path pathStorageRoot;

    private final Path reversePathStorageRoot;

    private final Path resourceStorageRoot;

    private final Path temporaryFileDirectory;

    private final String pathSeparator;

    private final Path tombstone;

    private final Path lockFilePath;

    @Inject
    public UnixFSUtils(@Named(STORAGE_ROOT_DIRECTORY) final Path storageRoot) {
        this.storageRoot = storageRoot;
        this.lockFilePath = storageRoot.resolve(LOCK_FILE_NAME);
        this.revisionTablePath = storageRoot.resolve(HEAD_FILE_NAME).toAbsolutePath().normalize();
        this.revisionPoolPath = storageRoot.resolve(REVISION_POOL_FILE_NAME).toAbsolutePath().normalize();
        this.transactionJournalPath = storageRoot.resolve(TRANSACTION_JOURNAL_FILE_NAME).toAbsolutePath().normalize();
        this.tombstone = storageRoot.resolve(TOMBSTONE_FILE_NAME).toAbsolutePath().normalize();
        this.pathStorageRoot = storageRoot.resolve(PATHS_DIRECTORY).toAbsolutePath().normalize();
        this.reversePathStorageRoot = storageRoot.resolve(REVERSE_DIRECTORY).toAbsolutePath().normalize();
        this.resourceStorageRoot = storageRoot.resolve(RESOURCES_DIRECTORY).toAbsolutePath().normalize();
        this.temporaryFileDirectory = storageRoot.resolve(TEMPORARY_DIRECTORY).toAbsolutePath().normalize();
        pathSeparator = pathStorageRoot.getFileSystem().getSeparator();
    }

    public Revision.Factory getRevisionFactory() {
        return revisionFactory;
    }

    @Inject
    public void setRevisionFactory(Revision.Factory revisionFactory) {
        this.revisionFactory = revisionFactory;
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
    public Revision<Path> findLatestForRevision(final Path directory,
                                                final Revision<?> revision,
                                                final LinkType linkType) {

        final boolean exists = exists(directory);
        final boolean isDirectory = isDirectory(directory);

        if (exists && !isDirectory) throw new IllegalArgumentException(directory + " must be a directory.");
        else if (!exists) return revision.withOptionalValue(Optional.empty());

        return doOperation(() -> Files
            .list(directory)
            .filter(linkType::matches))
            .map(path -> getRevisionFactory().create(linkType.stripExtensionToFilename(path)).withValue(path))
            .filter(r -> r.isBeforeOrSame(revision))
            .max(naturalOrder())
            .orElse(zero());

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
    public Stream<Revision<Path>> findRevisionsUpTo(final Path directory,
                                                    final Revision<?> revision,
                                                    final LinkType linkType) {

        final boolean exists = exists(directory);
        final boolean isDirectory = isDirectory(directory);

        if (exists && !isDirectory) throw new IllegalArgumentException(directory + " must be a directory.");
        else if (!exists) return Stream.empty();

        return doOperation(() -> Files
            .list(directory)
            .filter(linkType::matches))
            .map(path -> getRevisionFactory().create(linkType.stripExtensionToFilename(path)).withValue(path))
            .filter(r -> r.isBeforeOrSame(revision));

    }

    /**
     * Searches a directory for the tombstone at the supplied {@link Revision<?>}. If it exists, this returns a
     * {@link Revision<Path>} with a path indicating the tombstone. Otherwise, the resulting {@link Revision<Path>} will
     * not contain a value.
     *
     * @param fsPathDirectory the directory
     * @param revision the revision to search
     * @param linkType the link type to search
     * @return the {@link Revision<Path>}
     */
    public Revision<Path> findLatestTombstone(final Path fsPathDirectory,
                                              final Revision<?> revision,
                                              final LinkType linkType) {
        return findLatestForRevision(fsPathDirectory, revision, linkType).filter(this::isTombstone);
    }

    /**
     * Gets the symbolic link path to the {@link Revision}.
     *
     * @param parent the parent directory.
     * @param revision the revision to resolve.
     *
     * @return the resolved symbolic link path
     */
    public Path resolveSymlinkPath(final Path parent, final Revision<?> revision) {
        return parent.resolve(format("%s.%s", revision.getUniqueIdentifier(), REVISION_SYMBOLIC_LINK_SUFFIX));
    }

    /**
     * Resolves a revision file by appending the value of {@link #REVISION_SUFFIX} to the end of the file name.
     *
     * @param parent the parent {@link Path} owning the file
     * @param revision the {@link Revision<?>} to use when resolving the file name
     * @return the {@link Path} to the fully-resolved file with revision suffix
     */
    public Path resolveRevisionFilePath(final Path parent, final Revision<?> revision) {
        return parent.resolve(format("%s.%s", revision.getUniqueIdentifier(), REVISION_SUFFIX));
    }

    /**
     * Resolves a revision directory by appending the value of {@link #REVISION_SUFFIX} to the end of the file name.
     *
     * @param parent the parent {@link Path} owning the file
     *
     * @return the {@link Path} to the fully-resolved file with revision suffix
     */
    public Path resolveRevisionDirectoryPath(final Path parent) {
        return parent.resolve(REVERSE_DIRECTORY);
    }

    /**
     * Initializes the directory contents for all the necessary sub directories.
     */
    public void initialize() {

        doOperationV(() -> {
            createDirectories(getStorageRoot());
            createDirectories(getPathStorageRoot());
            createDirectories(getResourceStorageRoot());
            createDirectories(getTemporaryFileDirectory());
            createDirectories(getReversePathStorageRoot());
            if (!isRegularFile(tombstone, NOFOLLOW_LINKS)) createFile(tombstone);
        }, FatalException::new);

        final Set<FileSystem> fileSystemSet = new HashSet<>();

        fileSystemSet.add(tombstone.getFileSystem());
        fileSystemSet.add(getPathStorageRoot().getFileSystem());
        fileSystemSet.add(getRevisionPoolPath().getFileSystem());
        fileSystemSet.add(getRevisionTableFilePath().getFileSystem());
        fileSystemSet.add(getPathStorageRoot().getFileSystem());
        fileSystemSet.add(getResourceStorageRoot().getFileSystem());
        fileSystemSet.add(getTemporaryFileDirectory().getFileSystem());
        fileSystemSet.add(getTransactionJournalPath().getFileSystem());
        fileSystemSet.add(getRevisionTableFilePath().getFileSystem());

        if (fileSystemSet.size() > 1) {
            throw new IllegalArgumentException(format("%s %s and %s must share common filesystem.",
                pathStorageRoot,
                resourceStorageRoot,
                temporaryFileDirectory));
        }

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
        return doOperation(action, FatalException::new);
    }

    /**
     * Performs and operation that may throw an instance of {@link IOException}, and re-throws it wrapped inside of a
     * {@link InternalException}.
     *
     * @param action the action to perform
     * @param <T> the return type
     * @return the value returned from the {@link IOOperation<T>}
     */
    public <T, ExceptionT extends InternalException> T doOperation(
            final IOOperation<T> action,
            final Function<Throwable, ExceptionT> exceptionTFunction) throws ExceptionT{
        try {
            return action.perform();
        } catch (Exception ex) {
            logger.error("IOException Performing operation.", ex);
            throw exceptionTFunction.apply(ex);
        }
    }

    /**
     * Performs an IO Operation which may throw, catching the exception and wrapping it in the type specified in the
     * function.
     *
     * @param action the action to perfrom
     * @param <ExceptionT> the specified exception type
     */
    public <ExceptionT extends InternalException> void doOperationV(final IOOperationV action) {
        doOperationV(action, FatalException::new);
    }

    /**
     * Performs an IO Operation which may throw, catching the exception and wrapping it in the type specified in the
     * function.
     *
     * @param action the action to perfrom
     * @param exceptionTFunction a supplier to constrcut the exception
     * @param <ExceptionT> the specified exception type
     */
    public <ExceptionT extends InternalException> void doOperationV(
            final IOOperationV action,
            final Function<Throwable, ExceptionT> exceptionTFunction) {
        try {
            action.perform();
        } catch (IOException ex) {
            logger.error("IOException Performing operation.", ex);
            throw exceptionTFunction.apply(ex);
        }
    }

    /**
     * Gets the storage root directory.
     *
     * @return the storage root
     */
    public Path getStorageRoot() {
        return storageRoot;
    }

    /**
     * Returns tha {@link Path} to the directory holding the path mapping.
     * @return the {@link Path} to the path mapping
     */
    public Path getPathStorageRoot() {
        return pathStorageRoot;
    }

    /**
     * Returns tha {@link Path} to the directory holding the reverse directory mapping.
     * @return the {@link Path} to the path mapping
     */
    public Path getReversePathStorageRoot() {
        return reversePathStorageRoot;
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
     * A directory for temporary files which will eventually be linked to the main storage roots and will later
     * be deleted.
     *
     * @return the temporary file directory.
     */
    public Path getTemporaryFileDirectory() {
        return temporaryFileDirectory;
    }

    /**
     * Returns the path to the journal file.
     * @return the path to the journal file.
     */
    public Path getTransactionJournalPath() {
        return transactionJournalPath;
    }

    /**
     * Returns the path to the head file.
     * @return the path to the head file.
     */
    public Path getRevisionTableFilePath() {
        return revisionTablePath;
    }

    /**
     * Returns the path to the revision pool file.
     * @return the path to the revision pool file.
     */
    public Path getRevisionPoolPath() {
        return revisionPoolPath;
    }

    /**
     * Returns the path separator string associated with the filesystem used by the storage directory.
     *
     * @return the path separator.
     */
    public String getPathSeparator() {
        return pathSeparator;
    }

    /**
     * Writes a lock file in the storage root, throwing a {@link FatalException} if there is already a lock file. This
     * uses the {@link File#deleteOnExit()} facility to attempt to unlock the directoy regardless of exit, however this
     * uses shutdown hooks and is not guaranteed.
     */
    public void lockStorageRoot() {
        final Path created = doOperation(() -> createFile(lockFilePath), FatalException::new);
        created.toFile().deleteOnExit();
    }

    /**
     * Deletes the lock file in the storage root, throwing a {@link FatalException} if the was a problem unlocking.
     */
    public void unlockStorageRoot() {
        doOperationV(() -> deleteIfExists(lockFilePath), FatalException::new);
    }

    /**
     * Returns a {@link Path} to the path storage directory for the supplied {@link NodeId}.
     *
     * @param nodeId the {@link NodeId}
     *
     * @return the {@link Path} for the {@link NodeId}
     */
    public Path resolvePathStorageRoot(final NodeId nodeId) {
        return getPathStorageRoot().resolve(nodeId.asString());
    }

    /**
     * Returns true if the supplied path is a tombstone path.
     *
     * @param fsPath the {@link Path} to check for a tombstone.
     * @return true if the path represents a tombstone, false otherwise
     */
    public boolean isTombstone(final Path fsPath) {
        return doOperation(() -> isSameFile(tombstone, fsPath), FatalException::new);
    }

    /**
     * Flags the supplied directory for deletion. This essentially links a well-known file, ie the tombstone file, to
     * the revision. It is possible to later check that this revision is a tombstone by using
     * {@link #isTombstone(Path)}.
     *
     * @param directory the {@link Path} to the directory.
     * @param revision the {@link Revision<?>} at which to apply the tombstone.
     */
    public void tombstone(final Path directory, final Revision<?> revision) {
        if (!isDirectory(directory, NOFOLLOW_LINKS)) throw new IllegalArgumentException(directory + " is not a directory.");
        final Path destination = directory.resolve(revision.getUniqueIdentifier());
        doOperationV(() -> createLink(destination, tombstone), FatalException::new);
    }

    /**
     * Defines an operation which may throw an instance of {@link IOException}
     */
    @FunctionalInterface
    public interface IOOperationV {

        /**
         * Performs the operation.
         *
         * @return the calculated vallue of the operation.
         * @throws IOException for any reason.
         */
        void perform() throws IOException;

        default IOOperationV andThen(final IOOperationV next) {
            return () -> {
                try {
                    perform();
                } finally {
                    next.perform();
                }
            };
        }

        /**
         * Starts building an {@link IOOperationV}
         *
         * @return an instance of {@link IOOperationV} which does nothing.
         */
        static IOOperationV begin() {
            return () -> {};
        }

    }

    /**
     * Allocates a file in the temporary directory that can be atomically linked to a permanently stored file.
     *
     * @return the {@link Path} to the file.
     */
    public Path allocateTemporaryFile() {

        final Random random = ThreadLocalRandom.current();

        do {

            final StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < TEMP_NAME_LENGTH_CHARS; ++i) {
                final int index = random.nextInt(TEMP_FILE_CHARACTERS.length());
                stringBuilder.append(TEMP_FILE_CHARACTERS.charAt(index));
            }

            final Path temporaryFile = temporaryFileDirectory.resolve(stringBuilder.toString());

            try {
                return createFile(temporaryFile);
            } catch (FileAlreadyExistsException ex) {
                continue;
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        } while (true);

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
         * @return the calculated value of the operation.
         * @throws IOException for any reason.
         */
        T perform() throws IOException;

    }

    /**
     * Indicates the path type, including the extension.
     */
    public enum LinkType {

        /**
         * Indicates that the path is a directory.
         */
        DIRECTORY(DIRECTORY_SUFFIX, Files::isDirectory),

        /**
         * Indicates that the path is revision symbolic link to a directory.
         */
        REVISION_DIRECTORY(REVISION_SYMBOLIC_LINK_SUFFIX, Files::isSymbolicLink),

        /**
         * Indicates that the type is a hard link to a file (ie regular file).
         */
        REVISION_HARD_LINK(REVISION_SUFFIX, Files::isRegularFile),

        /**
         * Indicates that the type is a revision symlink.
         */
        REVISION_SYMBOLIC_LINK(REVISION_SYMBOLIC_LINK_SUFFIX, Files::isSymbolicLink);

        private final String extension;
        private final Predicate<Path> typePredicate;

        LinkType(final String extension, final Predicate<Path> typePredicate) {
            this.typePredicate = typePredicate;
            this.extension = format(".%s", extension);
        }

        /**
         * Gets the extension added to the last path component of the {@link Path}. This extension includes the '.'
         * character.
         *
         * @return the full extension
         */
        public String getExtension() {
            return extension;
        }

        /**
         * Strips the extension from the provided {@link Path}, throwing an exception if the path does not end with the
         * expected extension.
         *
         * @param path the {@link Path}
         * @return the stripped {@link Path} without the extension
         */
        public Path stripExtension(final Path path) {
            final String stripped = stripExtension(path.getFileName().toString());
            return path.resolveSibling(stripped);
        }

        /**
         * Strips the extension from the provided {@link Path}, throwing an exception if the path does not end with the
         * expected extension.
         *
         * @param path the {@link Path}
         * @return a {@link String} containing just the filename, sans extension
         */
        public String stripExtensionToFilename(final Path path) {
            return stripExtension(path.getFileName().toString());
        }

        /**
         * Strips the extension from the provided {@link String}, throwing an exception if the path does not end with
         * the expected extension.
         *
         * @param path the {@link String}
         * @return the stripped {@link Path}
         */
        public String stripExtension(final String path) {
            if (!path.endsWith(getExtension())) throw new IllegalArgumentException("Extension not present.");
            return path.substring(0, path.length() - extension.length());
        }

        /**
         * Tests if the supplied {@link Path} matches the expected extension and is of the correct type.
         *
         * @param path the {@link Path}
         * @return true if the {@link Path} matches
         */
        public boolean matches(final Path path) {
            return typePredicate.test(path) && path.toString().endsWith(extension);
        }

    }

}
