package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.PessimisticLocking;
import com.namazustudios.socialengine.rt.transact.ReadWriteTransaction;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteBufferByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class XodusReadWriteTransaction implements ReadWriteTransaction {

    private static final Logger logger = LoggerFactory.getLogger(XodusReadWriteTransaction.class);

    private final int blockSize;

    private final ResourceStores stores;

    private final Transaction transaction;

    private final PessimisticLocking pessimisticLocking;

    private final XodusReadOnlyTransaction readOnlyTransaction;

    private final Publisher<XodusReadWriteTransaction> onClose = new SimplePublisher<>();

    public XodusReadWriteTransaction(
            final int blockSize,
            final ResourceStores stores,
            final Transaction transaction,
            final PessimisticLocking pessimisticLocking) {
        this.stores = stores;
        this.blockSize = blockSize;
        this.transaction = transaction;
        this.pessimisticLocking = pessimisticLocking;
        this.readOnlyTransaction = new XodusReadOnlyTransaction(stores, transaction);
    }

    @Override
    public WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId) throws TransactionConflictException {
        doLinkNew(path, resourceId);
        return new BlockWritableChannel(resourceId);
    }

    @Override
    public WritableByteChannel updateResource(final ResourceId resourceId) throws TransactionConflictException {

        getPessimisticLocking().lock(resourceId);

        if (!deleteBlocks(resourceId)) {
            throw new ResourceNotFoundException("No such resource exists with resource id " + resourceId);
        }

        return new BlockWritableChannel(resourceId);

    }

    private boolean deleteBlocks(final ResourceId resourceId) {

        try (final var cursor = getStores().getResourceBlocks().openCursor(transaction)) {

            final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);
            if (cursor.getSearchKeyRange(resourceIdKey) == null) return false;

            do {
                cursor.deleteCurrent();
            } while (cursor.getNext() && XodusUtil.isMatchingBlockKey(resourceIdKey, cursor.getKey()));

            return true;

        }

    }

    @Override
    public void linkNewResource(final Path path, final ResourceId resourceId) throws TransactionConflictException {
        doLinkNew(path, resourceId);
    }

    private void doLinkNew(final Path path, final ResourceId resourceId) throws TransactionConflictException {

        final var pathKey = new ArrayByteIterable(path.toByteArray());
        final var resourceIdKey = new ArrayByteIterable(resourceId.asBytes());

        if (getStores().getPaths().get(getTransaction(), pathKey) != null) {
            throw new DuplicateException("Resource exists at path " + path);
        } else if (getStores().getReversePaths().get(getTransaction(), resourceIdKey) != null) {
            throw new DuplicateException("Resource exists with resource id " + resourceId);
        }

        getPessimisticLocking().lock(path);
        getPessimisticLocking().lock(resourceId);

        if (getStores().getPaths().put(getTransaction(), pathKey, resourceIdKey)) {
            logger.debug("Added new mapping {} -> {}", path, resourceId);
        } else {
            logger.error("Could not add new mapping {} -> {}", path, resourceId);
        }

        if (getStores().getReversePaths().put(getTransaction(), resourceIdKey, pathKey)) {
            logger.debug("Added new reverse mapping {} -> {}", resourceId, path);
        } else {
            logger.error("Could not add new reverse mapping {} -> {}", resourceId, path);
        }

    }

    @Override
    public void linkExistingResource(final ResourceId sourceResourceId, final Path destination) throws TransactionConflictException {
        doLinkExisting(destination, sourceResourceId);
    }

    private void doLinkExisting(final Path path, final ResourceId resourceId) throws TransactionConflictException {

        final var pathKey = new ArrayByteIterable(path.toByteArray());
        final var resourceIdKey = new ArrayByteIterable(resourceId.asBytes());

        if (getStores().getPaths().get(getTransaction(), pathKey) != null) {
            throw new DuplicateException("Resource exists at path " + path);
        } else if (getStores().getReversePaths().get(getTransaction(), resourceIdKey) != null) {
            throw new ResourceNotFoundException("Resource does not exist " + resourceId);
        }

        getPessimisticLocking().lock(path);
        getPessimisticLocking().lock(resourceId);

        if (getStores().getPaths().put(getTransaction(), pathKey, resourceIdKey)) {
            logger.debug("Added existing mapping {} -> {}", path, resourceId);
        } else {
            logger.error("Could not add existing mapping {} -> {}", path, resourceId);
        }

        if (getStores().getReversePaths().put(getTransaction(), resourceIdKey, pathKey)) {
            logger.debug("Added existing reverse mapping {} -> {}", resourceId, path);
        } else {
            logger.error("Could not add existing reverse mapping {} -> {}", resourceId, path);
        }

    }

    @Override
    public ResourceService.Unlink unlinkPath(final Path path) throws TransactionConflictException {

        final var pathKey = XodusUtil.pathKey(path);

        try (var pathsCursor = getStores().getPaths().openCursor(getTransaction());
             var reversePathsCursor = getStores().getReversePaths().openCursor(getTransaction())) {

            final var resourceIdKey = pathsCursor.getSearchKey(pathKey);
            if (resourceIdKey == null) throw new ResourceNotFoundException("No resource exists for path: " + path);

            final var resourceId = XodusUtil.resourceId(resourceIdKey);

            getPessimisticLocking().lock(path);
            getPessimisticLocking().lock(resourceId);

            pathsCursor.deleteCurrent();

            if (reversePathsCursor.getSearchBoth(resourceIdKey, pathKey) && reversePathsCursor.deleteCurrent()) {
                logger.debug("Deleted reverse {} -> {}", path, resourceId);
            } else {
                logger.error("Reverse path mapping broken {} -> {}", path, resourceId);
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

        final var paths = list(path).limit(max).collect(toList());

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

    }

    @Override
    public List<ResourceId> removeResources(final Path path, int max) throws TransactionConflictException {
        return null;
    }

    @Override
    public void commit() {

    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        return getReadOnlyTransaction().exists(resourceId);
    }

    @Override
    public Stream<ResourceService.Listing> list(final Path path) {
        return getReadOnlyTransaction().list(path);
    }

    @Override
    public ResourceId getResourceId(final Path path) {
        return getReadOnlyTransaction().getResourceId(path);
    }

    @Override
    public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {
        return getReadOnlyTransaction().loadResourceContents(resourceId);
    }

    @Override
    public void close() {
        onClose.publish(this);
    }

    public int getBlockSize() {
        return blockSize;
    }

    public ResourceStores getStores() {
        return stores;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public PessimisticLocking getPessimisticLocking() {
        return pessimisticLocking;
    }

    public XodusReadOnlyTransaction getReadOnlyTransaction() {
        return readOnlyTransaction;
    }

    private class BlockWritableChannel implements WritableByteChannel {

        private final ResourceId resourceId;

        boolean open = false;

        long sequence = 0;

        final byte[] array = new byte[getBlockSize()];

        final ByteBuffer buffer = ByteBuffer.wrap(array);

        final Subscription onCloseSubscription = onClose.subscribe(t -> this.close());

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
            getStores().getResourceBlocks().put(transaction, blockKey, block);
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
