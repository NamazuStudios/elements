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
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
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

    private final Transaction transaction;

    private final PessimisticLocking pessimisticLocking;

    private final VirtualFileSystem virtualFileSystem;

    private final XodusResourceStores xodusResourceStores;

    private final XodusReadOnlyTransaction xodusReadOnlyTransaction;

    public XodusReadWriteTransaction(
            final NodeId nodeId,
            final XodusResourceStores stores,
            final VirtualFileSystem virtualFileSystem,
            final Transaction transaction,
            final PessimisticLocking pessimisticLocking) {
        if (transaction.isReadonly()) throw new IllegalArgumentException("Must use read-write transaction.");
        this.virtualFileSystem = virtualFileSystem;
        this.xodusResourceStores = stores;
        this.transaction = transaction;
        this.pessimisticLocking = pessimisticLocking;
        this.xodusReadOnlyTransaction = new XodusReadOnlyTransaction(nodeId, stores, virtualFileSystem, transaction);
        this.xodusReadOnlyTransaction.onClose(t -> getPessimisticLocking().unlock());
        this.xodusReadOnlyTransaction.onClose(t -> {
            if (!transaction.isFinished()) {
                transaction.revert();
            }
        });
    }

    @Override
    public NodeId getNodeId() {
        return getXodusReadOnlyTransaction().getNodeId();
    }

    @Override
    public WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId) throws TransactionConflictException, IOException {

        getXodusReadOnlyTransaction().check();

        final var qualified = check(path);
        doLinkNew(qualified, resourceId);
        return new BlockWritableChannel(resourceId);

    }

    @Override
    public WritableByteChannel updateResource(final ResourceId resourceId) throws TransactionConflictException, IOException {

        getXodusReadOnlyTransaction().check();
        check(resourceId);

        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        if (getXodusResourceStores().getReversePaths().get(getTransaction(), resourceIdKey) == null) {
            throw new ResourceNotFoundException("Resource does not exist: " + resourceId);
        }

        getPessimisticLocking().lock(resourceId);

        return new BlockWritableChannel(resourceId);

    }

    private boolean deleteFile(final ResourceId resourceId) {
        final var deletedFile = virtualFileSystem.deleteFile(getTransaction(), resourceId.toString());
        return deletedFile != null;
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
            if (resourceIdKey == null)
                throw new ResourceNotFoundException("No resource exists for path: " + qualified);

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

                if (deleteFile(resourceId)) {
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

        }

        deleteFile(resourceId);

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

        if (getTransaction().flush()) {
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

    private class BlockWritableChannel implements WritableByteChannel {

        boolean open = true;

        private final jetbrains.exodus.vfs.File file;

        private final OutputStream outputStream;

        private final ResourceId resourceId;

        private final ArrayList<byte[]> buffer = new ArrayList<byte[]>();

        private final Subscription onCloseSubscription = getXodusReadOnlyTransaction().onClose(t -> {
            try {
                this.close();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        public BlockWritableChannel(ResourceId resourceId) throws IOException {

            file = virtualFileSystem.openFile(getTransaction(), resourceId.toString(), true);
            outputStream = virtualFileSystem.writeFile(getTransaction(), file);

            this.resourceId = resourceId;
        }

        @Override
        public int write(final ByteBuffer src) throws IOException {

            final var bytes = new byte[src.remaining()];

            src.get(bytes);
            buffer.add(bytes);

            return bytes.length;
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

                int totalLength = 0;

                for (byte[] entry : buffer) {
                    totalLength += entry.length;
                }

                final ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength);
                totalLength = 0;

                for (byte[] entry : buffer) {
                    final int length = entry.length;

                    byteBuffer.put(entry, totalLength, length);
                }

                byteBuffer.rewind();

                final var bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);

                outputStream.write(bytes);
                outputStream.close();
            }
        }

    }

}
