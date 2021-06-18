package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ExclusiveReadWriteTransaction;
import com.namazustudios.socialengine.rt.transact.PessimisticLocking;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.vfs.VirtualFileSystem;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.xodus.XodusResourceStores.ALL_STORES;

public class XodusExclusiveReadWriteTransaction implements ExclusiveReadWriteTransaction {

    private final Transaction transaction;

    private final XodusResourceStores xodusResourceStores;

    private final XodusReadWriteTransaction xodusReadWriteTransaction;

    public XodusExclusiveReadWriteTransaction(
            final NodeId nodeId,
            final long blockSize,
            final XodusResourceStores stores,
            final VirtualFileSystem virtualFileSystem,
            final Transaction transaction,
            final PessimisticLocking pessimisticLocking) {

        if (!transaction.isExclusive() && transaction.isReadonly())
            throw new IllegalArgumentException("Must use read-write transaction.");

        this.transaction = transaction;
        this.xodusResourceStores = stores;
        this.xodusReadWriteTransaction = new XodusReadWriteTransaction(
            nodeId,
            blockSize,
            stores,
            virtualFileSystem,
            transaction,
            pessimisticLocking);

    }

    @Override
    public NodeId getNodeId() {
        return getXodusReadWriteTransaction().getNodeId();
    }

    @Override
    public boolean exists(ResourceId resourceId) {
        return getXodusReadWriteTransaction().exists(resourceId);
    }

    @Override
    public Stream<ResourceService.Listing> list(Path path) {
        return getXodusReadWriteTransaction().list(path);
    }

    @Override
    public ResourceId getResourceId(Path path) {
        return getXodusReadWriteTransaction().getResourceId(path);
    }

    @Override
    public ReadableByteChannel loadResourceContents(ResourceId resourceId) {
        return getXodusReadWriteTransaction().loadResourceContents(resourceId);
    }

    @Override
    public WritableByteChannel saveNewResource(Path path, ResourceId resourceId) throws TransactionConflictException, IOException {
        return getXodusReadWriteTransaction().saveNewResource(path, resourceId);
    }

    @Override
    public WritableByteChannel updateResource(ResourceId resourceId) throws TransactionConflictException, IOException {
        return getXodusReadWriteTransaction().updateResource(resourceId);
    }

    @Override
    public void linkNewResource(Path path, ResourceId resourceId) throws TransactionConflictException {
        getXodusReadWriteTransaction().linkNewResource(path, resourceId);
    }

    @Override
    public void linkExistingResource(ResourceId sourceResourceId, Path destination) throws TransactionConflictException {
        getXodusReadWriteTransaction().linkExistingResource(sourceResourceId, destination);
    }

    @Override
    public ResourceService.Unlink unlinkPath(Path path) throws TransactionConflictException {
        return getXodusReadWriteTransaction().unlinkPath(path);
    }

    @Override
    public List<ResourceService.Unlink> unlinkMultiple(Path path, int max) throws TransactionConflictException {
        return getXodusReadWriteTransaction().unlinkMultiple(path, max);
    }

    @Override
    public void removeResource(ResourceId resourceId) throws TransactionConflictException {
        getXodusReadWriteTransaction().removeResource(resourceId);
    }

    @Override
    public List<ResourceId> removeResources(Path path, int max) throws TransactionConflictException {
        return getXodusReadWriteTransaction().removeResources(path, max);
    }

    @Override
    public Stream<ResourceId> removeAllResources() {

        getXodusReadWriteTransaction()
            .getXodusReadOnlyTransaction()
            .check();

        final var resourceIds = new ArrayList<ResourceId>();

        try (var pathsCursor = getXodusResourceStores().getPaths().openCursor(getTransaction());
             var reversePathsCursor = getXodusResourceStores().getReversePaths().openCursor(getTransaction());
             var resourceBlockCursor = getXodusResourceStores().getResourceBlocks().openCursor(getTransaction())
        ) {

            if (reversePathsCursor.getNext()) {
                do {
                    final var resourceIdKey = reversePathsCursor.getKey();
                    final var resourceId = XodusUtil.resourceId(resourceIdKey);
                    resourceIds.add(resourceId);
                } while (reversePathsCursor.getNextNoDup());
            }

            while (pathsCursor.getNext()) pathsCursor.deleteCurrent();
            while (resourceBlockCursor.getNext()) resourceBlockCursor.deleteCurrent();

        }

        return resourceIds.stream();

    }

    @Override
    public void truncate() {
        ALL_STORES.forEach(store -> getEnvironment().truncateStore(store, getTransaction()));
    }

    @Override
    public void commit() throws TransactionConflictException {
        getXodusReadWriteTransaction().commit();
    }

    @Override
    public void close() {
        getXodusReadWriteTransaction().close();
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Environment getEnvironment() {
        return getTransaction().getEnvironment();
    }

    public XodusResourceStores getXodusResourceStores() {
        return xodusResourceStores;
    }

    public XodusReadWriteTransaction getXodusReadWriteTransaction() {
        return xodusReadWriteTransaction;
    }

}
