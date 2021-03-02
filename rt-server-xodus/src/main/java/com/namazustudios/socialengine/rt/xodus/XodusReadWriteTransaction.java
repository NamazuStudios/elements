package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ReadWriteTransaction;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import jetbrains.exodus.env.Transaction;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.stream.Stream;

public class XodusReadWriteTransaction implements ReadWriteTransaction {

    private final ResourceStores stores;

    private final Transaction transaction;

    private final XodusReadOnlyTransaction readOnlyTransaction;

    public XodusReadWriteTransaction(ResourceStores stores, Transaction transaction) {
        this.stores = stores;
        this.transaction = transaction;
        this.readOnlyTransaction = new XodusReadOnlyTransaction(stores, transaction);
    }

    @Override
    public WritableByteChannel saveNewResource(Path path, ResourceId resourceId) throws IOException, TransactionConflictException {
        return null;
    }

    @Override
    public WritableByteChannel updateResource(ResourceId resourceId) throws IOException, TransactionConflictException {
        return null;
    }

    @Override
    public void linkNewResource(Path path, ResourceId resourceId) throws TransactionConflictException {

    }

    @Override
    public void linkExistingResource(ResourceId sourceResourceId, Path destination) throws TransactionConflictException {

    }

    @Override
    public ResourceService.Unlink unlinkPath(Path path) throws TransactionConflictException {
        return null;
    }

    @Override
    public List<ResourceService.Unlink> unlinkMultiple(Path path, int max) throws TransactionConflictException {
        return null;
    }

    @Override
    public void removeResource(ResourceId resourceId) throws TransactionConflictException {

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

}
