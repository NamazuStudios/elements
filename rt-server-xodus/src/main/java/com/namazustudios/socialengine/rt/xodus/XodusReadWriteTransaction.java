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

        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        final var pathKey = new ArrayByteIterable(path.toByteArray());
        final var resourceIdKey = new ArrayByteIterable(resourceId.asBytes());

        if (stores.getPaths().get(transaction, pathKey) != null) {
            throw new DuplicateException("Resource exists at path " + path);
        } else if (stores.getReversePaths().get(transaction, resourceIdKey) != null) {
            throw new DuplicateException("Resource exists with resource id " + resourceId);
        }

        return new BlockWritableChannel(resourceId);

    }

    @Override
    public WritableByteChannel updateResource(final ResourceId resourceId) throws IOException, TransactionConflictException {

        pessimisticLocking.lock(resourceId);

        final var resourceIdKey = new ArrayByteIterable(resourceId.asBytes());

        if (stores.getReversePaths().get(transaction, resourceIdKey) == null) {
            throw new ResourceNotFoundException("No such resource exists with resource id " + resourceId);
        }

        deleteBlocks(resourceId);

        return new BlockWritableChannel(resourceId);

    }

    private void deleteBlocks(final ResourceId resourceId) {

        final var cursor = stores.getResourceBlocks().openCursor(transaction);
        final var resourceIdKey = XodusUtil.resourceIdKey(resourceId);

        do {
            cursor.getSearchKey()
        } while (cursor.getNext())

    }

    @Override
    public void linkNewResource(final Path path, final ResourceId resourceId) throws TransactionConflictException {

    }

    @Override
    public void linkExistingResource(final ResourceId sourceResourceId, final Path destination) throws TransactionConflictException {

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
        return readOnlyTransaction.exists(resourceId);
    }

    @Override
    public Stream<ResourceService.Listing> list(Path path) {
        return readOnlyTransaction.list(path);
    }

    @Override
    public ResourceId getResourceId(Path path) {
        return readOnlyTransaction.getResourceId(path);
    }

    @Override
    public ReadableByteChannel loadResourceContents(ResourceId resourceId) throws IOException {
        return readOnlyTransaction.loadResourceContents(resourceId);
    }

    @Override
    public void close() {
        readOnlyTransaction.close();
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
            stores.getResourceBlocks().put(transaction, blockKey, block);
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
