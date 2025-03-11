package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.id.HasNodeId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.transact.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.emptySet;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

/**
 * A collection of useful utility routines when accessing the filesystem.
 */
public class UnixFSUtils {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSUtils.class);

    public static final String UNIXFS_STORAGE_ROOT_DIRECTORY = "dev.getelements.elements.rt.transact.unix.fs.root";

    public static final String LOCK_FILE_NAME = "lock";

    public static final String TOMBSTONE_FILE_NAME = "tombstone";

    public static final String TRANSACTION_JOURNAL_FILE_NAME = "journal";

    public static final String TRANSACTION_JOURNAL_DIRECTORY = "journal.d";

    public static final String NODE_DIRECTORY = "node";

    public static final String GARBAGE_DIRECTORY = "garbage";

    public static final String PATHS_DIRECTORY = "paths";

    public static final String TASKS_DIRECTORY = "tasks";

    public static final String RESOURCES_DIRECTORY = "resources";

    public static final String REVERSE_PATHS_DIRECTORY = "reverse";

    public static final int TEMP_NAME_LENGTH_CHARS = 128;

    public static final String TASK_EXTENSION = "tsk";

    public static final String RESOURCE_EXTENSION = "rsc";

    public static final String REVERSE_PATH_EXTENSION = "rev";

    public static final String TRANSACTION_EXTENSION = "txn";

    public static final String EXTENSION_REGEX = format("\\.(%s|%s|%s)$", TASK_EXTENSION, RESOURCE_EXTENSION, REVERSE_PATH_EXTENSION);

    public static final Pattern EXTENSION_PATTERN = compile(EXTENSION_REGEX);

    public static final String TEMP_FILE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHI0123456789";

    private final Path tombstone;

    private final Path storageRoot;

    private final Path transactionJournalFilePath;

    private final Path transactionJournalDirectoryPath;

    private final Path nodeStorageRoot;

    private final Path garbageDirectory;

    private final String pathSeparator;

    private final Path lockFilePath;

    private final UnixFSChecksumAlgorithm checksumAlgorithm;

    @Inject
    public UnixFSUtils(final UnixFSChecksumAlgorithm checksumAlgorithm,
                       @Named(UNIXFS_STORAGE_ROOT_DIRECTORY) final Path storageRoot) {
        this.checksumAlgorithm = checksumAlgorithm;
        this.pathSeparator = storageRoot.getFileSystem().getSeparator();
        this.storageRoot = storageRoot.toAbsolutePath().normalize();
        this.lockFilePath = storageRoot.resolve(LOCK_FILE_NAME).toAbsolutePath().normalize();
        this.tombstone = storageRoot.resolve(TOMBSTONE_FILE_NAME).toAbsolutePath().normalize();
        this.nodeStorageRoot = storageRoot.resolve(NODE_DIRECTORY).toAbsolutePath().normalize();
        this.garbageDirectory = storageRoot.resolve(GARBAGE_DIRECTORY).toAbsolutePath().normalize();
        this.transactionJournalFilePath = storageRoot.resolve(TRANSACTION_JOURNAL_FILE_NAME).toAbsolutePath().normalize();
        this.transactionJournalDirectoryPath = storageRoot.resolve(TRANSACTION_JOURNAL_DIRECTORY).toAbsolutePath().normalize();
    }

    /**
     * Initializes the directory contents for all the necessary sub directories.
     */
    public void initialize() {

        doOperationV(() -> {

            createDirectories(getStorageRoot());
            createDirectories(getNodeStorageRoot());
            createDirectories(getGarbageDirectory());
            createDirectories(getTransactionJournalDirectoryPath());

            try {
                createFile(getTombstone());
                logger.trace("Created tombstone file {}", getTombstone());
            } catch (FileAlreadyExistsException ex) {
                logger.trace("Tombstone already exists {}", getTombstone());
            }

        }, FatalException::new);

        final var fileSystemSet = new HashSet<>();
        fileSystemSet.add(getTombstone().getFileSystem());
        fileSystemSet.add(getNodeStorageRoot().getFileSystem());
        fileSystemSet.add(getGarbageDirectory().getFileSystem());
        fileSystemSet.add(getTransactionJournalFilePath().getFileSystem());
        fileSystemSet.add(getTransactionJournalDirectoryPath().getFileSystem());

        final var paths = Stream.of(
                getTombstone(),
                getStorageRoot(),
                getNodeStorageRoot(),
                getGarbageDirectory(),
                getTransactionJournalFilePath(),
                getTransactionJournalDirectoryPath()
        ).map(Objects::toString).collect(joining());

        if (fileSystemSet.size() > 1) {
            throw new IllegalArgumentException(format("[%s] must share common filesystem.", paths));
        }

        cleanupGarbage();

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
     * Defines an operation which may throw an instance of {@link IOException}
     */
    @FunctionalInterface
    public interface IOOperationV {

        /**
         * Performs the operation.
         * 
         * @throws IOException for any reason.
         */
        void perform() throws IOException;

    }

    /**
     * Performs an IO Operation which may throw, catching the exception and wrapping it in the type specified in the
     * function.
     *
     * @param action the action to perfrom
     */
    public void doOperationV(final IOOperationV action) {
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
     * Gets the configured and preferred checksum algorithm.
     *
     * @return the {@link UnixFSChecksumAlgorithm} to use
     */
    public UnixFSChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
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
     * Gets the storage root for node storage.
     *
     * @return the {@link Path}
     */
    public Path getNodeStorageRoot() {
        return nodeStorageRoot;
    }

    /**
     * Gets the directory for temporary storge of garbage data.
     *
     * @return the garbage directory
     */
    public Path getGarbageDirectory() {
        return garbageDirectory;
    }

    /**
     * Returns the path to the journal file.
     * @return the path to the journal file.
     */
    public Path getTransactionJournalFilePath() {
        return transactionJournalFilePath;
    }

    /**
     * Returns the path to the journal directory.
     *
     * @return the path to the journal file.
     */
    public Path getTransactionJournalDirectoryPath() {
        return transactionJournalDirectoryPath;
    }

    /**
     * Gets the {@link Path} to the tombstone marker file.
     *
     * @return the {@link Path} to the tombstone file.
     */
    public Path getTombstone() {
        return tombstone;
    }

    /**
     * Gets the {@link Path} to the lock file.
     *
     * @return the {@link Path} to the lock file.
     */
    public Path getLockFilePath() {
        return lockFilePath;
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
     * Checks that the supplied {@link Path} is part of the datastore.
     *
     * @param path the {@link Path}
     * @return the supplied {@link Path} unchanged
     */
    public Path check(Path path) {

        if (!path.startsWith(getStorageRoot())) {
            throw new IllegalArgumentException(format("%s is not part of datastore at %s", path, getStorageRoot()));
        }

        return path;
    }

    /**
     * Writes a lock file in the storage root, throwing a {@link FatalException} if there is already a lock file. This
     * uses the {@link File#deleteOnExit()} facility to attempt to unlock the directoy regardless of exit, however this
     * uses shutdown hooks and is not guaranteed.
     */
    public void lockStorageRoot() {
        final Path created = doOperation(() -> createFile(getLockFilePath()), FatalException::new);
        created.toFile().deleteOnExit();
    }

    /**
     * Deletes the lock file in the storage root, throwing a {@link FatalException} if the was a problem unlocking.
     */
    public void unlockStorageRoot() {
        doOperationV(() -> deleteIfExists(getLockFilePath()), FatalException::new);
    }

    /**
     * Returns a {@link Path} to the path storage directory for the supplied {@link NodeId}.
     *
     * @param hasNodeId the {@link HasNodeId}
     *
     * @return the {@link Path} for the {@link HasNodeId}
     */
    public Path resolveNodeStorageRoot(final HasNodeId hasNodeId) {
        return hasNodeId
                .getOptionalNodeId()
                .map(nodeId -> getNodeStorageRoot().resolve(nodeId.asString()))
                .orElseThrow(() -> new IllegalArgumentException(format("%s must nave a node id", hasNodeId)))
                .toAbsolutePath();
    }

    /**
     * Returns a {@link Path} to the path storage directory for the supplied {@link NodeId}.
     *
     * @param hasNodeId the {@link HasNodeId}
     *
     * @return the {@link Path} for the {@link HasNodeId}
     */
    public Path resolvePathStorageRoot(final HasNodeId hasNodeId) {
        return resolveNodeStorageRoot(hasNodeId).resolve(PATHS_DIRECTORY);
    }

    /**
     * Returns a {@link Path} to the task storage directory for the supplied {@link NodeId}.
     *
     * @return the {@link Path} for the {@link NodeId}
     */
    public Path resolveTaskStorageRoot(final HasNodeId hasNodeId) {
        return resolveNodeStorageRoot(hasNodeId).resolve(TASKS_DIRECTORY);
    }

    /**
     * Returns a {@link Path} to the resource storage directory for the supplied {@link NodeId}.
     *
     * @return the {@link Path} for the {@link NodeId}
     */
    public Path resolveResourceStorageRoot(final HasNodeId hasNodeId) {
        return resolveNodeStorageRoot(hasNodeId).resolve(RESOURCES_DIRECTORY);
    }

    /**
     * Resolves the reverse paths storage directory.
     *
     * @return the {@link Path} to the directory.
     */
    public Path resolveReversePathStorageRoot(final HasNodeId hasNodeId) {
        return resolveNodeStorageRoot(hasNodeId).resolve(REVERSE_PATHS_DIRECTORY);
    }

    /**
     * Marks the supplied {@link Path} as a tombstone.
     *
     * @param path the path to mark
     */
    public void markTombstone(final Path path) {
        check(path);
        doOperationV(() -> createLink(path, tombstone));
    }

    /**
     * Checks if the supplied FS Path is a regular file and not a tombstone.
     *
     * @param hasFilesystemPath if the path is a regular file.
     * @return true if regular file
     */
    public boolean isRegularFile(final UnixFSHasFilesystemPath hasFilesystemPath) {
        return isRegularFile(hasFilesystemPath.getFilesystemPath());
    }

    /**
     * Checks if the supplied {@link Path} is a regular file.
     *
     * @param path the path to check
     * @return true if the path is a tombstone, false otherwise
     */
    public boolean isRegularFile(final Path path) {
        check(path);
        return doOperation(() -> Files.isRegularFile(path, NOFOLLOW_LINKS) && !isTombstone(path));
    }

    /**
     * Checks if the supplied {@link Path} is a tombstone.
     *
     * @param path the path to check
     * @return true if the path is a tombstone, false otherwise
     */
    public boolean isTombstone(final Path path) {
        check(path);
        return doOperation(() -> exists(path, NOFOLLOW_LINKS) && isSameFile(getTombstone(), path));
    }

    /**
     * Recursively removes the object at the supplied {@link Path}.
     *
     * @param path the path.
     */
    public void rmrf(final Path path) {

        check(path);

        doOperationV(() -> {

            try (var directory = (SecureDirectoryStream<Path>) Files.newDirectoryStream(path)) {

                for (final var child : directory) {
                    rmrf(child, directory);
                }

                directory.deleteDirectory(path);

            }
        });

    }

    private void rmrf(final Path path, final SecureDirectoryStream<Path> directory) throws IOException {
        if (Files.isDirectory(path)) {

            try (var childDirectory = directory.newDirectoryStream(path, NOFOLLOW_LINKS)) {
                for (var childPath : childDirectory) {
                    rmrf(childPath, childDirectory);
                }
            }

            directory.deleteDirectory(path);

        } else if (Files.isRegularFile(path, NOFOLLOW_LINKS)) {
            directory.deleteFile(path);
        }
    }

    /**
     * Prune the supplied {@link Path}. This will delete all empty directories and subdirectories of the path provided
     * that they are empty.
     *
     * @param path the path to prune
     */
    public void prune(final Path path) {

        check(path);

        doOperationV(() -> {

            if (!isDirectory(path, NOFOLLOW_LINKS)) {
                return;
            }

            try (final var root = (SecureDirectoryStream<Path>)newDirectoryStream(getStorageRoot());
                 final var toPrune = root.newDirectoryStream(path, NOFOLLOW_LINKS)) {
                if (prune(path, toPrune)) {
                    try {
                        toPrune.deleteDirectory(path);
                    } catch (NoSuchFileException ex) {
                        logger.info("Caught exception pruning path. Skipping.", ex);
                    }
                }
            }

        });

    }

    private boolean prune(final Path path, final SecureDirectoryStream<Path> directory) throws IOException {
        if (isDirectory(path)) {

            try (var childDirectory = directory.newDirectoryStream(path)) {
                for (var childPath : childDirectory) {
                    if (prune(childPath, childDirectory)) {
                        directory.deleteDirectory(childPath);
                    }
                }
            }

            return Files.list(path).findAny().isEmpty();

        } else {
            return false;
        }
    }

    /**
     * Allocates a journal file.
     *
     * @param transactionId the transaction ID
     * @return the {@link Path} to the journal file
     */
    public Path getTransactionFilePath(final String transactionId) {
        return getTransactionJournalDirectoryPath().resolve(format("%s.%s", transactionId, TRANSACTION_EXTENSION));
    }

    /**
     * Allocates a directory in the garbage directory.
     *
     * @return the {@link Path} to the file.
     */
    public Path allocateGarbageDirectory() {

        final Random random = ThreadLocalRandom.current();

        do {

            final StringBuilder stringBuilder = new StringBuilder("garbage-");

            for (int i = 0; i < TEMP_NAME_LENGTH_CHARS; ++i) {
                final int index = random.nextInt(TEMP_FILE_CHARACTERS.length());
                stringBuilder.append(TEMP_FILE_CHARACTERS.charAt(index));
            }

            final Path garbageDirectory = getGarbageDirectory().resolve(stringBuilder.toString());

            try {
                return createDirectories(garbageDirectory);
            } catch (FileAlreadyExistsException ex) {
                logger.trace("Garbage name already in use {} ", garbageDirectory);
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        } while (true);

    }

    /**
     * Cleans up any garbage in the garbage directory.
     */
    public void cleanupGarbage() {
        doOperationV(() -> {
            try (var directory = (SecureDirectoryStream<Path>) Files.newDirectoryStream(getGarbageDirectory())) {
                for (final var child : directory) {
                    rmrf(child, directory);
                }
            }
        });
    }

    /**
     * Cleans up the garbage in the supplied {@link Path}.
     *
     * @param garbageDirectory the garbage to clean up
     */
    public void cleanupGarbage(final Path garbageDirectory) {

        if (!garbageDirectory.startsWith(getGarbageDirectory())) {
            throw new IllegalArgumentException(format("%s is not garbage.", garbageDirectory));
        } else if (!isDirectory(garbageDirectory)) {
            throw new IllegalArgumentException(format("%s is not a directory.", garbageDirectory));
        }

        rmrf(garbageDirectory);

    }

    /**
     * A result for the {@link #commit(UnixFSHasFilesystemPath, String)} operation.
     */
    public enum CommitResult {

        /**
         * The supplied path was updated to a new version.
         */
        UPDATED,

        /**
         * The supplied path was ignored because the source file didn't exist.
         */
        IGNORED,

        /**
         * The supplied path was deleted because the update was the tombstone.
         */
        DELETED,

    }

    /**
     * Commits the supplied file, provided the transaction ID.
     * @param hasFilesystemPath a mapping of a type which has a filesystem path
     * @param transactionId the transaction ID
     */
    public CommitResult commit(final UnixFSHasFilesystemPath hasFilesystemPath, final String transactionId) {

        final var source = hasFilesystemPath.getFilesystemPath(transactionId);
        final var destination = hasFilesystemPath.getFilesystemPath();

        return doOperation(() -> {
            if (isTombstone(source) || isTombstone(destination)) {
                deleteIfExists(source);
                deleteIfExists(destination);
                return CommitResult.DELETED;
            } else if (isRegularFile(source)) {
                move(source, destination, ATOMIC_MOVE, REPLACE_EXISTING);
                return CommitResult.UPDATED;
            } else {
                logger.debug("Unable to move file {} -> {}. Ignoring.", source, destination);
                return CommitResult.IGNORED;
            }
        });

    }

    /**
     * Cleans up the object with the transaction ID.
     *
     * @param hasFilesystemPath a mapping of a type which has a filesystem path
     * @param transactionId the transaction ID
     */
    public void cleanup(final UnixFSHasFilesystemPath hasFilesystemPath, final String transactionId) {
        final var toCleanup = hasFilesystemPath.getFilesystemPath(transactionId);
        doOperationV(() -> deleteIfExists(toCleanup));
    }

    /**
     * Appends the file extension to the supplied path.
     *
     * @param path the base path
     * @param extension the extension to append
     * @return the fully-formed path
     */
    public Path appendExtension(final Path path, final String extension) {
        check(path);
        final var filename = format("%s.%s", path.getFileName(), extension);
        return path.resolveSibling(filename);
    }

    /**
     * Checks if the path matches the supplied extension.
     *
     * @param path the {@link dev.getelements.elements.sdk.cluster.path.Path}
     * @param extension the extension
     * @return true if matches, otherwise false
     */
    public boolean isMatchingExtension(final Path path, final String extension) {
        final var regex = format(".+?\\.(%s)$", extension);
        return path.toString().matches(regex);
    }

    /**
     * Returns a {@link Path} with the extension stripped. The extension must be one of the known extensions in this
     * class, or else this will throw an instance of {@link IllegalArgumentException}.
     *
     * @param path the path
     * @return the path, with file extension stripped.
     */
    public Path stripExtension(final Path path) {
        check(path);
        final var filename = path.getFileName().toString();
        final var matcher = EXTENSION_PATTERN.matcher(filename);
        final var sibling = matcher.replaceAll("");
        final var stripped = path.resolveSibling(sibling);
        return stripped;
    }

    /**
     * Appends the {@link UnixFSUtils#TASK_EXTENSION} extension to the supplied path.
     *
     * @param path the base path
     * @return the fully-formed path
     */
    public Path appendTaskExtension(final Path path) {
        return appendExtension(path, TASK_EXTENSION);
    }

    /**
     * Appends the {@link UnixFSUtils#RESOURCE_EXTENSION} extension to the supplied path.
     *
     * @param path the base path
     * @return the fully-formed path
     */
    public Path appendResourceExtension(final Path path) {
        return appendExtension(path, RESOURCE_EXTENSION);
    }

    /**
     * Appends the {@link UnixFSUtils#REVERSE_PATH_EXTENSION} extension to the supplied path.
     *
     * @param path the base path
     * @return the fully-formed path
     */
    public Path appendReversePathExtension(final Path path) {
        return appendExtension(path, REVERSE_PATH_EXTENSION);
    }

    /**
     * Safely lists the contents of the supplied directory. If the list process encounters a missing file, for example
     * if it had been deleted by the garbage collector, then the stream will simply skip that file. This ensures that
     * it will be possible to work only with live fields when iterating the contents of a directory.
     *
     * Internally this uses a spliterator and lazily fetches as needed.
     *
     * Otherwise, this must behave in a manner identical to {@link Files#list(Path)}
     *
     * @param directory the directory to list
     * @return a {@link Stream<Path>}
     */
    public Stream<Path> list(final Path directory) {
        final var spliterator = new FileWalkSpliterator(directory, 1);
        return stream(spliterator, false);
    }

    /**
     * Safely lists the contents of the supplied directory as well as its children. If the walk process encounters a
     * missing file, for example if it had been deleted by the garbage collector, then the stream will simply skip that
     * file. This ensures that it will be possible to work only with live fields when iterating the contents of a
     * directory.
     *
     * Internally this uses a spliterator and lazily fetches as needed.
     *
     * Otherwise this must behave in a manner identical to {@link Files#walk(Path, FileVisitOption...)}
     *
     * @param directory the directory to list
     * @return a {@link Stream<Path>}
     */
    public Stream<Path> walk(final Path directory) {
        final var spliterator = new FileWalkSpliterator(directory, Integer.MAX_VALUE);
        return concat(Stream.of(directory), stream(spliterator, false));
    }

    private class FileWalkSpliterator extends Spliterators.AbstractSpliterator<Path> {

        private final int max;

        private final Path root;

        private final Queue<Path> pending = new LinkedList<>();

        private final Queue<Path> directories = new LinkedList<>();

        private final FileVisitor<Path> visitor = new FileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                final var depth = dir.getNameCount() - root.getNameCount();
                return depth >= max ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                if (attrs.isDirectory()) directories.add(file);
                pending.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
                if (exc instanceof NoSuchFileException) {
                    logger.debug("Failed to visit file. Ignoring.", exc);
                    return FileVisitResult.CONTINUE;
                } else if (exc != null) {
                    throw new FatalException(exc);
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                if (exc instanceof NoSuchFileException) {
                    logger.debug("Failed to visit file. Ignoring.", exc);
                    return FileVisitResult.CONTINUE;
                } else if (exc != null) {
                    throw new FatalException(exc);
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }

        };

        public FileWalkSpliterator(final Path directory, final int maxDepth) {
            super(0, DISTINCT | IMMUTABLE);
            if (maxDepth < 0) throw new IllegalArgumentException("Must have depth of 0 or more.");
            this.max = maxDepth;
            this.root = directory;
            this.directories.add(directory);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super Path> action) {
            var result = pending.poll();
            if (result == null) result = doList();
            if (result != null) action.accept(result);
            return result != null;
        }

        private Path doList() {

            final var directory = directories.poll();
            if (directory == null) return null;

            return doOperation(() -> {
                walkFileTree(directory, emptySet(), 1, visitor);
                return pending.poll();
            }, FatalException::new);

        }

    }

}
