package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.PessimisticLocking;
import com.namazustudios.socialengine.rt.transact.ReadWriteTransaction;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import jetbrains.exodus.ByteBufferByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.FileByteIterable;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Integer.min;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toList;

public class XodusReadWriteTransaction implements ReadWriteTransaction {

    private static final Logger logger = LoggerFactory.getLogger(XodusReadWriteTransaction.class);

    private static final String TEMP_FILE_PREFIX = XodusReadWriteTransaction.class.getSimpleName();

    private static final String TEMP_FILE_SUFFIX = "resource";

    private final long blockSize;

    private final Transaction transaction;

    private final PessimisticLocking pessimisticLocking;

    private final XodusResourceStores xodusResourceStores;

    private final XodusReadOnlyTransaction xodusReadOnlyTransaction;

    private final List<File> temporaryFiles = new ArrayList<>();

    public XodusReadWriteTransaction(
            final NodeId nodeId,
            final long blockSize,
            final XodusResourceStores xodusResourceStores,
            final Transaction transaction,
            final PessimisticLocking pessimisticLocking) {
        if (transaction.isReadonly()) throw new IllegalArgumentException("Must use read-write transaction.");
        this.xodusResourceStores = xodusResourceStores;
        this.blockSize = blockSize;
        this.transaction = transaction;
        this.pessimisticLocking = pessimisticLocking;
        this.xodusReadOnlyTransaction = new XodusReadOnlyTransaction(nodeId, xodusResourceStores, transaction);
        this.xodusReadOnlyTransaction.onClose(t -> getPessimisticLocking().unlock());
        this.xodusReadOnlyTransaction.onClose(t -> temporaryFiles.forEach(File::delete));
    }

    @Override
    public NodeId getNodeId() {
        return getXodusReadOnlyTransaction().getNodeId();
    }

    @Override
    public WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId) throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();

        final var qualified = check(path);
        doLinkNew(qualified, resourceId);
        return new BlockWritableChannel(resourceId);

    }

    @Override
    public WritableByteChannel updateResource(final ResourceId resourceId) throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();
        check(resourceId);

        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        if (getXodusResourceStores().getReversePaths().get(getTransaction(), resourceIdKey) == null) {
            throw new ResourceNotFoundException("Resource does not exist: " + resourceId);
        }

        getPessimisticLocking().lock(resourceId);

        if (deleteBlocks(resourceIdKey)) {
            logger.debug("Deleted blocks for {}", resourceId);
        } else {
            logger.debug("No blocks found when deleting {}", resourceId);
        }

        return new BlockWritableChannel(resourceId);

    }

    private boolean deleteBlocks(final ResourceId resourceId) {
        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);
        return deleteBlocks(resourceIdKey);
    }

    boolean deleteBlocks(final ByteIterable resourceIdKey) {

        try (final var cursor = getXodusResourceStores().getResourceBlocks().openCursor(transaction)) {

            if (cursor.getSearchKeyRange(resourceIdKey) == null) return false;

            do {
                cursor.deleteCurrent();
            } while (cursor.getNext() && XodusUtil.isMatchingBlockKey(resourceIdKey, cursor.getKey()));

            return true;

        }

    }

    @Override
    public void linkNewResource(final Path path, final ResourceId resourceId) throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();

        final var qualified = check(path);
        check(resourceId);
        doLinkNew(qualified, resourceId);

    }

    private void doLinkNew(final Path path, final ResourceId resourceId) throws TransactionConflictException {

        check(resourceId);

        final var qualified = check(path);
        final var pathKey = XodusUtil.pathKey(qualified);
        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        if (getXodusResourceStores().getPaths().get(getTransaction(), pathKey) != null) {
            throw new DuplicateException("Resource exists at path " + qualified);
        } else if (getXodusResourceStores().getReversePaths().get(getTransaction(), resourceIdKey) != null) {
            throw new DuplicateException("Resource exists with resource id " + resourceId);
        }

        getPessimisticLocking().lock(qualified);
        getPessimisticLocking().lock(resourceId);

        if (getXodusResourceStores().getPaths().put(getTransaction(), pathKey, resourceIdKey)) {
            logger.debug("Added new mapping {} -> {}", qualified, resourceId);
        } else {
            logger.error("Could not add new mapping {} -> {}", qualified, resourceId);
        }

        if (getXodusResourceStores().getReversePaths().put(getTransaction(), resourceIdKey, pathKey)) {
            logger.debug("Added new reverse mapping {} -> {}", resourceId, qualified);
        } else {
            logger.error("Could not add new reverse mapping {} -> {}", resourceId, qualified);
        }

    }

    @Override
    public void linkExistingResource(final ResourceId sourceResourceId, final Path destination) throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();
        check(sourceResourceId);

        final var qualified = check(destination);
        doLinkExisting(qualified, sourceResourceId);

    }

    private void doLinkExisting(final Path path, final ResourceId resourceId) throws TransactionConflictException {

        final var qualified = check(path);
        final var pathKey = XodusUtil.pathKey(qualified);
        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        if (getXodusResourceStores().getPaths().get(getTransaction(), pathKey) != null) {
            throw new DuplicateException("Resource exists at path " + qualified);
        } else if (getXodusResourceStores().getReversePaths().get(getTransaction(), resourceIdKey) == null) {
            throw new ResourceNotFoundException("Resource does not exist " + resourceId);
        }

        getPessimisticLocking().lock(qualified);
        getPessimisticLocking().lock(resourceId);

        if (getXodusResourceStores().getPaths().put(getTransaction(), pathKey, resourceIdKey)) {
            logger.debug("Added existing mapping {} -> {}", qualified, resourceId);
        } else {
            logger.error("Could not add existing mapping {} -> {}", qualified, resourceId);
        }

        if (getXodusResourceStores().getReversePaths().put(getTransaction(), resourceIdKey, pathKey)) {
            logger.debug("Added existing reverse mapping {} -> {}", resourceId, qualified);
        } else {
            logger.error("Could not add existing reverse mapping {} -> {}", resourceId, qualified);
        }

    }

    @Override
    public ResourceService.Unlink unlinkPath(final Path path) throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();

        final var qualified = check(path);
        final var pathKey = XodusUtil.pathKey(qualified);

        try (var pathsCursor = getXodusResourceStores().getPaths().openCursor(getTransaction());
             var reversePathsCursor = getXodusResourceStores().getReversePaths().openCursor(getTransaction())) {

            final var resourceIdKey = pathsCursor.getSearchKey(pathKey);
            if (resourceIdKey == null) throw new ResourceNotFoundException("No resource exists for path: " + qualified);

            final var resourceId = XodusUtil.resourceId(resourceIdKey);

            getPessimisticLocking().lock(qualified);
            getPessimisticLocking().lock(resourceId);

            pathsCursor.deleteCurrent();

            if (reversePathsCursor.getSearchBoth(resourceIdKey, pathKey)) {
                if (reversePathsCursor.deleteCurrent()) {
                    logger.debug("Deleted reverse {} -> {}", resourceId, qualified);
                } else {
                    logger.error("Unable to delete reverse mapping {} -> {}", resourceId, qualified);
                }
            } else {
                logger.error("Reverse path mapping broken {} -> {}", resourceId, qualified);
            }

            if (reversePathsCursor.getSearchKey(resourceIdKey) == null) {

                logger.debug("Resource {} is not linked elsewhere. Deleting blocks if present.", resourceId);

                if (deleteBlocks(resourceId)) {
                    logger.debug("Deleted blocks for {}", resourceId);
                } else {
                    logger.debug("No blocks to delete for {} ", resourceId);
                }

                return new ResourceService.Unlink() {
                    @Override
                    public ResourceId getResourceId() {
                        return resourceId;
                    }

                    @Override
                    public boolean isRemoved() {
                        return true;
                    }
                };

            } else {

                logger.debug("Resource {} is linked elsewhere. Preserving blocks.", resourceId);

                return new ResourceService.Unlink() {
                    @Override
                    public ResourceId getResourceId() {
                        return resourceId;
                    }

                    @Override
                    public boolean isRemoved() {
                        return false;
                    }
                };

            }

        }

    }

    @Override
    public List<ResourceService.Unlink> unlinkMultiple(final Path path, final int max) throws TransactionConflictException {

        final var qualified = check(path);
        final var paths = list(qualified).limit(max).collect(toList());

        for (final var listing : paths) {
            getPessimisticLocking().lock(listing.getPath());
            getPessimisticLocking().lock(listing.getResourceId());
        }

        final var results = new ArrayList<ResourceService.Unlink>();

        for (final var listing : paths) {
            results.add(unlinkPath(listing.getPath()));
        }

        return results;

    }

    @Override
    public void removeResource(final ResourceId resourceId) throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();

        check(resourceId);

        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        try (var pathsCursor = getXodusResourceStores().getPaths().openCursor(getTransaction());
             var reversePathsCursor = getXodusResourceStores().getReversePaths().openCursor(getTransaction())) {

            if (reversePathsCursor.getSearchKey(resourceIdKey) == null) {
                throw new ResourceNotFoundException("Resource with id not found: " + resourceId);
            }

            final var paths = new ArrayList<Path>();
            final var pathKeys = new ArrayList<ByteIterable>();

            getPessimisticLocking().lock(resourceId);

            do {
                final var pathKey = reversePathsCursor.getValue();
                final var path = XodusUtil.path(pathKey);
                paths.add(path);
                pathKeys.add(pathKey);
            } while (reversePathsCursor.getNextDup());

            final var pathsItr = paths.iterator();
            final var pathKeysItr = pathKeys.iterator();

            do {

                final var path = pathsItr.next();
                final var pathKey = pathKeysItr.next();

                if (pathsCursor.getSearchBoth(pathKey, resourceIdKey)) {

                    if (pathsCursor.deleteCurrent()) {
                        logger.debug("Deleted path mapping {} -> {}.", path, resourceId);
                    } else {
                        logger.error("Could not delete path mapping {} -> {}.", path, resourceId);
                    }

                } else {
                    logger.error("Reverse path mapping broken {} -> {}", resourceId, path);
                }

                if (reversePathsCursor.getSearchBoth(resourceIdKey, pathKey)) {
                    if (reversePathsCursor.deleteCurrent()) {
                        logger.debug("Deleted reverse path mapping {} -> {}.", resourceId, path);
                    } else {
                        logger.error("Could not delete {} -> {} from reverse paths.", resourceId, path);
                    }
                } else {
                    logger.error("Reverse path mapping broken {} -> {}", resourceId, path);
                }

            } while (pathsItr.hasNext() && pathKeysItr.hasNext());

            if (deleteBlocks(resourceIdKey)) {
                logger.debug("Deleted resource blocks for {}", resourceId);
            } else {
                logger.debug("No blocks exist for for {}", resourceId);
            }

        }

        deleteBlocks(resourceId);

    }

    @Override
    public List<ResourceId> removeResources(final Path path, int max) throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();

        final var qualified = check(path);
        final var paths = list(qualified).limit(max).collect(toList());

        for (final var listing : paths) {
            getPessimisticLocking().lock(listing.getPath());
            getPessimisticLocking().lock(listing.getResourceId());
        }

        final var results = new ArrayList<ResourceId>();

        for (final var listing : paths) {
            removeResource(listing.getResourceId());
            results.add(listing.getResourceId());
        }

        return results;

    }

    @Override
    public void commit() throws TransactionConflictException {

        getXodusReadOnlyTransaction().check();

        if (getTransaction().commit()) {
            logger.debug("Successfully committed transaction.");
        } else {
            throw new TransactionConflictException();
        }

    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        return getXodusReadOnlyTransaction().exists(resourceId);
    }

    @Override
    public Stream<ResourceService.Listing> list(final Path path) {
        return getXodusReadOnlyTransaction().list(path);
    }

    @Override
    public ResourceId getResourceId(final Path path) {
        return getXodusReadOnlyTransaction().getResourceId(path);
    }

    @Override
    public ReadableByteChannel loadResourceContents(final ResourceId resourceId) {
        return getXodusReadOnlyTransaction().loadResourceContents(resourceId);
    }

    @Override
    public void close() {
        getXodusReadOnlyTransaction().close();
    }

    public long getBlockSize() {
        return blockSize;
    }

    public XodusResourceStores getXodusResourceStores() {
        return xodusResourceStores;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public PessimisticLocking getPessimisticLocking() {
        return pessimisticLocking;
    }

    public XodusReadOnlyTransaction getXodusReadOnlyTransaction() {
        return xodusReadOnlyTransaction;
    }

    public File allocateTemporaryFile() throws IOException {
        final var temporaryFile = Files.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX).toFile();
        temporaryFiles.add(temporaryFile);
        return temporaryFile;
    }

    private class BlockWritableChannel implements WritableByteChannel {

        boolean open = true;

        private File file = null;

        private FileChannel fileChannel = null;

        private FileByteIterable fileByteIterable = null;

        private long sequence = 0;

        private final ResourceId resourceId;

        private final ByteBuffer buffer = ByteBuffer.allocate( (int) getBlockSize());

        private final Subscription onCloseSubscription = getXodusReadOnlyTransaction().onClose(t -> {
            try {
                this.close();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        public BlockWritableChannel(ResourceId resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public int write(final ByteBuffer src) throws IOException {

            if (!open) throw new IllegalStateException();

            final var initial = buffer.remaining();

            final var oldLimit = src.limit();
            final var newLimit = src.position() + min(buffer.remaining(), src.remaining());

            try {
                return !buffer.put(src.limit(newLimit)).hasRemaining()
                    ? flushToFileIfNecessary(initial)
                    : initial - buffer.remaining();
            } finally {
                src.limit(oldLimit);
            }

        }

        private int flushToFileIfNecessary(final int initial) throws IOException {

            // If we go beyond what a single block can hold, we dump the contents to a file such that it
            // may be read later block by block. This prevents loading potentially large files into memory
            // while committing the transaction.

            final int written = initial - buffer.remaining();

            // This lazily creates the file.

            if (file == null) {
                file = allocateTemporaryFile();
                fileChannel = FileChannel.open(file.toPath(), READ, WRITE);
                fileByteIterable = new FileByteIterable(file);
            }

            // Dumps the whole buffer to the file, and rewinds the buffer for the next set of write operations

            buffer.rewind();
            while (buffer.hasRemaining()) fileChannel.write(buffer);
            buffer.rewind();

            sequence++;

            return written;

        }

        private void flushRemainingBuffer() {
            final var block = new ByteBufferByteIterable(buffer.flip());
            final var blockKey = XodusUtil.resourceBlockKey(resourceId, sequence++);
            getXodusResourceStores().getResourceBlocks().put(transaction, blockKey, block);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            if (open) {

                // Flags the buffer as closed
                open = false;
                onCloseSubscription.unsubscribe();

                // If we created a temporary file, we must flush the contents of that file to disk before the
                // transaction may proceed as it may reference parts of the file.
//                if (file != null) fileChannel.close();

                if (file != null) {

                    final ByteBuffer mapped = fileChannel.map(READ_ONLY, 0, fileChannel.size());
                    fileChannel.close();

                    for (int blockSequence = 0; blockSequence < sequence; ++blockSequence) {

                        // Calculates the offset and length based on the block size.
                        final int offset = (int) (getBlockSize() * blockSequence);
                        final int limit  = (int)  (offset + getBlockSize());

                        // And finally carves apart the iterable into a sub-iterable
                        final var block = new ByteBufferByteIterable(mapped.position(offset).limit(limit).slice());
                        final var blockKey = XodusUtil.resourceBlockKey(resourceId, blockSequence);
                        getXodusResourceStores().getResourceBlocks().put(transaction, blockKey, block);

                    }

                }

                // Whatever is left in the buffer will be written to at the end of the sequence
                flushRemainingBuffer();

            }
        }

    }


}
