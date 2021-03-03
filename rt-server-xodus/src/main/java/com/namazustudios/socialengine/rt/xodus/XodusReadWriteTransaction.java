package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.stream.Stream;

public class XodusReadWriteTransaction implements ReadWriteTransaction {

    private final int blockSize;

    private final ResourceStores stores;

    private final Transaction transaction;

    private final PessimisticLocking pessimisticLocking;

    private final XodusReadOnlyTransaction readOnlyTransaction;

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

        pessimisticLocking.lock(resourceId);

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

        if (getStores().getPaths().get(transaction, pathKey) != null) {
            throw new DuplicateException("Resource exists at path " + path);
        } else if (getStores().getReversePaths().get(transaction, resourceIdKey) != null) {
            throw new DuplicateException("Resource exists with resource id " + resourceId);
        }

        getPessimisticLocking().lock(path);
        getPessimisticLocking().lock(resourceId);
        getStores().getPaths().put(getTransaction(), pathKey, resourceIdKey);
        getStores().getReversePaths().put(getTransaction(), resourceIdKey, pathKey);

    }

    @Override
    public void linkExistingResource(final ResourceId sourceResourceId, final Path destination) throws TransactionConflictException {
        doLinkExisting(destination, sourceResourceId);
    }

    private void doLinkExisting(final Path path, final ResourceId resourceId) throws TransactionConflictException {

        final var pathKey = new ArrayByteIterable(path.toByteArray());
        final var resourceIdKey = new ArrayByteIterable(resourceId.asBytes());

        if (getStores().getPaths().get(transaction, pathKey) != null) {
            throw new DuplicateException("Resource exists at path " + path);
        } else if (getStores().getReversePaths().get(transaction, resourceIdKey) != null) {
            throw new ResourceNotFoundException("Resource does not exist " + resourceId);
        }

        getPessimisticLocking().lock(path);
        getPessimisticLocking().lock(resourceId);
        getStores().getPaths().put(getTransaction(), pathKey, resourceIdKey);
        getStores().getReversePaths().put(getTransaction(), resourceIdKey, pathKey);

    }

    @Override
    public ResourceService.Unlink unlinkPath(final Path path) throws TransactionConflictException {
        return null;
    }

    @Override
    public List<ResourceService.Unlink> unlinkMultiple(final Path path, final int max) throws TransactionConflictException {
        return null;
    }

    @Override
    public void removeResource(final ResourceId resourceId) throws TransactionConflictException {

    }

    @Override
    public List<ResourceId> removeResources(Path path, int max) throws TransactionConflictException {
        return null;
    }

    @Override
    public void commit() {

    }

    @Override
    public boolean exists(ResourceId resourceId) {
        return getReadOnlyTransaction().exists(resourceId);
    }

    @Override
    public Stream<ResourceService.Listing> list(Path path) {
        return getReadOnlyTransaction().list(path);
    }

    @Override
    public ResourceId getResourceId(Path path) {
        return getReadOnlyTransaction().getResourceId(path);
    }

    @Override
    public ReadableByteChannel loadResourceContents(ResourceId resourceId) throws IOException {
        return getReadOnlyTransaction().loadResourceContents(resourceId);
    }

    @Override
    public void close() {
        // TODO Close
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

        final byte[] array = new byte[blockSize];

        final ByteBuffer buffer = ByteBuffer.wrap(array);

        public BlockWritableChannel(ResourceId resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public int write(final ByteBuffer src) {
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
                doFlush();
            }
        }

    }

}
