package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.RevisionDataStore.PendingRevisionChange;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class DefaultTransactionalResourceServicePersistence implements TransactionalResourceServicePersistence {

    private static final Logger logger = getLogger(DefaultTransactionalResourceServicePersistence.class);

    private final Lock lock;

    private final Lock exclusiveLock;

    private final RevisionDataStore revisionDataStore;

    private final TransactionJournal transactionJournal;

    private final TransactionalPersistenceContext transactionalPersistenceContext;

    @Inject
    public DefaultTransactionalResourceServicePersistence(
            final RevisionDataStore revisionDataStore,
            final TransactionJournal transactionJournal,
            final TransactionalPersistenceContext transactionalPersistenceContext) {

        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.lock = rwLock.readLock();
        this.exclusiveLock = rwLock.readLock();

        this.revisionDataStore = revisionDataStore;
        this.transactionJournal = transactionJournal;
        this.transactionalPersistenceContext = transactionalPersistenceContext;

    }

    @Override
    public void start() {
        getTransactionalPersistenceContext().start();
    }

    @Override
    public void stop() {
        try {
            getTransactionalPersistenceContext().stop();
        } catch (Exception ex) {
            logger.error("Caught exception closing {}", getTransactionalPersistenceContext(), ex);
        }
    }

    @Override
    public ReadOnlyTransaction openRO(final NodeId nodeId) {
        try {
            lock.lock();
            final RevisionDataStore.LockedRevision revision = getRevisionDataStore().lockLatestReadUncommitted();
            return new DefaultReadOnlyTransaction(nodeId, revision);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ReadWriteTransaction openRW(final NodeId nodeId) {
        try {
            lock.lock();
            final TransactionJournal.MutableEntry entry = getTransactionJournal().newMutableEntry(nodeId);
            final RevisionDataStore.LockedRevision revision = getRevisionDataStore().lockLatestReadUncommitted();
            return new DefaultReadWriteTransaction(nodeId, revision, entry);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public com.namazustudios.socialengine.rt.transact.ExclusiveReadWriteTransaction openExclusiveRW(final NodeId nodeId) {
        try {
            exclusiveLock.lock();
            final TransactionJournal.MutableEntry entry = getTransactionJournal().newMutableEntry(nodeId);
            final RevisionDataStore.LockedRevision revision = getRevisionDataStore().lockLatestReadUncommitted();
            return new ExclusiveDefaultReadWriteTransaction(nodeId, revision, entry);
        } finally {
            exclusiveLock.unlock();
        }
    }

    public RevisionDataStore getRevisionDataStore() {
        return revisionDataStore;
    }

    public TransactionJournal getTransactionJournal() {
        return transactionJournal;
    }

    public TransactionalPersistenceContext getTransactionalPersistenceContext() {
        return transactionalPersistenceContext;
    }

    private boolean existsAt(final Revision<?> revision,
                             final ResourceId resourceId) {

        final Revision<Boolean> exists = getRevisionDataStore()
            .getResourceIndex()
            .existsAt(revision.comparableTo(), resourceId);

        return exists.getValue().isPresent() && exists.getValue().get();

    }

    private Stream<ResourceService.Listing> listAt(final NodeId nodeId, final Revision<?> revision, final Path path) {

        final NodeId resolvedNodeId = path
            .getOptionalNodeId()
            .orElse(nodeId);

        final Revision<Stream<ResourceService.Listing>> listingRevision = getRevisionDataStore()
            .getPathIndex()
            .list(resolvedNodeId, revision.comparableTo(), path);

        return listingRevision.getValue().orElseGet(Stream::empty);

    }

    private ResourceId getResourceIdAt(final NodeId nodeId, final Revision<?> revision, final Path path) {

        final NodeId resolvedNodeId = path
            .getOptionalNodeId()
            .orElse(nodeId);

        return getRevisionDataStore()
            .getPathIndex()
            .getRevisionMap(resolvedNodeId)
            .getValueAt(revision.comparableTo(), path)
            .getValue()
            .orElseThrow(ResourceNotFoundException::new);

    }

    private ReadableByteChannel loadResourceContentsAt(final Revision<?> revision,
                                                       final ResourceId resourceId) throws IOException {

        final Revision<ReadableByteChannel> readableByteChannelRevision = getRevisionDataStore()
            .getResourceIndex()
            .loadResourceContentsAt(revision.comparableTo(), resourceId);

        return readableByteChannelRevision.getValue().orElseThrow(() -> new ResourceNotFoundException());

    }

    private class DefaultReadOnlyTransaction implements ReadOnlyTransaction {

        private final NodeId nodeId;

        private final RevisionDataStore.LockedRevision revision;

        public DefaultReadOnlyTransaction(final NodeId nodeId,
                                          final RevisionDataStore.LockedRevision revision) {
            this.nodeId = nodeId;
            this.revision = revision;
        }

        @Override
        public Revision<?> getReadRevision() {
            return revision.getRevision();
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            return existsAt(revision.getRevision(), resourceId);
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {
            return listAt(nodeId, revision.getRevision(), path);
        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return getResourceIdAt(nodeId, revision.getRevision(), path);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {
            return loadResourceContentsAt(revision.getRevision(), resourceId);
        }

        @Override
        public void close() {
            revision.close();
        }

    }

    private class DefaultReadWriteTransaction implements ReadWriteTransaction {

        private final Lock lock;

        private final RevisionDataStore.LockedRevision revision;

        protected final NodeId nodeId;

        protected final TransactionJournal.MutableEntry entry;

        public DefaultReadWriteTransaction(final NodeId nodeId,
                                           final RevisionDataStore.LockedRevision revision,
                                           final TransactionJournal.MutableEntry entry) {
            this(exclusiveLock, nodeId, revision, entry);
        }

        public DefaultReadWriteTransaction(final Lock lock,
                                           final NodeId nodeId,
                                           final RevisionDataStore.LockedRevision revision,
                                           final TransactionJournal.MutableEntry entry) {
            this.entry = entry;
            this.revision = revision;
            this.nodeId = nodeId;
            this.lock = lock;
            lock.lock();
        }

        @Override
        public Revision<?> getReadRevision() {
            return revision.getRevision();
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            return existsAt(revision.getRevision(), resourceId);
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {

            final Revision<Stream<ResourceService.Listing>> indexed = getRevisionDataStore()
                .getPathIndex()
                .list(nodeId, revision.getRevision(), path);

            return indexed.getValue().orElseGet(Stream::empty);

        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return getResourceIdAt(nodeId, revision.getRevision(), path);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId)
                throws IOException {
            return loadResourceContentsAt(revision.getRevision(), resourceId);
        }

        @Override
        public WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId)
                throws IOException, TransactionConflictException {
            return entry.saveNewResource(path, resourceId);
        }

        @Override
        public void linkNewResource(final Path path, final ResourceId id)
                throws TransactionConflictException {
            entry.linkNewResource(id, path);
        }

        @Override
        public void linkExistingResource(final ResourceId sourceResourceId, final Path destination)
                throws TransactionConflictException {
            entry.linkExistingResource(sourceResourceId, destination);
        }

        @Override
        public ResourceService.Unlink unlinkPath(final Path path) throws TransactionConflictException {
            return entry.unlinkPath(path);
        }

        @Override
        public List<ResourceService.Unlink> unlinkMultiple(final Path path, final int max) throws TransactionConflictException {
            return entry.unlinkMultiple(path, max);
        }

        @Override
        public void removeResource(final ResourceId resourceId) throws TransactionConflictException {
            entry.removeResource(resourceId);
        }

        @Override
        public List<ResourceId> removeResources(final Path path, final int max) throws TransactionConflictException {
            return entry.removeResources(path, max);
        }

        @Override
        public void commit() {
            try (final PendingRevisionChange pending = getRevisionDataStore().beginRevisionUpdate()) {
                write(pending);
                pending.update();
            }
        }

        @Override
        public void close() {
            FinallyAction.begin()
                .then(() -> entry.close())
                .then(() -> lock.unlock())
                .then(() -> revision.close())
            .perform();
        }

        private void write(final PendingRevisionChange pending) {

            final Revision<?> revision = pending.getRevision();

            try {
                entry.commit(revision);
                pending.apply(entry);
            } catch (Exception ex) {
                pending.fail();
                throw new FatalException(ex);
            } finally {
                pending.cleanup(entry);
            }

        }

    }

    private class ExclusiveDefaultReadWriteTransaction
            extends DefaultReadWriteTransaction
            implements ExclusiveReadWriteTransaction {

        public ExclusiveDefaultReadWriteTransaction(final NodeId nodeId,
                                                    final RevisionDataStore.LockedRevision revision,
                                                    final TransactionJournal.MutableEntry entry) {
            super(exclusiveLock, nodeId, revision, entry);
        }

        @Override
        public Stream<ResourceId> removeAllResources() {
            return getRevisionDataStore().removeAllResources();
        }

    }

}
