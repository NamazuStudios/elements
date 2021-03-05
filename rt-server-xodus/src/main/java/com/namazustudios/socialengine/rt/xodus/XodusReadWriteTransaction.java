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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toList;

public class XodusReadWriteTransaction implements ReadWriteTransaction {

    private static final Logger logger = LoggerFactory.getLogger(XodusReadWriteTransaction.class);

    private final long blockSize;

    private final Transaction transaction;

    private final PessimisticLocking pessimisticLocking;

    private final XodusResourceStores xodusResourceStores;

    private final XodusReadOnlyTransaction xodusReadOnlyTransaction;

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

    private boolean deleteBlocks(final ByteIterable resourceIdKey) {

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

    private class BlockWritableChannel implements WritableByteChannel {

        private final ResourceId resourceId;

        boolean open = true;

        long sequence = 0;

        final byte[] array = new byte[(int)getBlockSize()];

        final ByteBuffer buffer = ByteBuffer.wrap(array);

        final Subscription onCloseSubscription = getXodusReadOnlyTransaction().onClose(t -> this.close());

        public BlockWritableChannel(ResourceId resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public int write(final ByteBuffer src) {
            if (!open) throw new IllegalStateException();
            final var initial = buffer.remaining();
            return !buffer.put(src).hasRemaining() ? flush(initial) : initial - buffer.remaining();
        }

        private int flush(final int initial) {
            final int written = initial - buffer.remaining();
            doFlush();
            return written;
        }

        private void doFlush() {
            final var block = new ByteBufferByteIterable(buffer.flip());
            final var blockKey = XodusUtil.resourceBlockKey(resourceId, sequence++);
            getXodusResourceStores().getResourceBlocks().put(transaction, blockKey, block);
            buffer.clear();
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            if (open) {

                open = false;
                onCloseSubscription.unsubscribe();

                doFlush();
                getPessimisticLocking().unlock();

            }
        }

    }


}
