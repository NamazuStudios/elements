package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.*;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class UnixFSTransactionalResourceServicePersistence implements TransactionalResourceServicePersistence {

    private static final Logger logger = getLogger(UnixFSTransactionalResourceServicePersistence.class);

    private final UnixFSRevisionPool unixFSRevisionPool;

    private final UnixFSRevisionDataStore unixFSRevisionDataStore;

    private final UnixFSTransactionJournal unixFSTransactionJournal;

    @Inject
    public UnixFSTransactionalResourceServicePersistence(
            final UnixFSRevisionPool unixFSRevisionPool,
            final UnixFSRevisionDataStore unixFSRevisionDataStore,
            final UnixFSTransactionJournal unixFSTransactionJournal) {
        this.unixFSRevisionPool = unixFSRevisionPool;
        this.unixFSRevisionDataStore = unixFSRevisionDataStore;
        this.unixFSTransactionJournal = unixFSTransactionJournal;
    }

    @Override
    public ReadOnlyTransaction openRO(final NodeId nodeId) {
        final UnixFSRevision<?> revision = getUnixFSRevisionDataStore().getCurrentRevision();
        final UnixFSJournalEntry entry = getUnixFSTransactionJournal().newSnapshotEntry(nodeId);
        return new UnixFSReadOnlyTransaction(nodeId, revision, entry);
    }

    @Override
    public ReadWriteTransaction openRW(final NodeId nodeId) {
        final UnixFSRevision<?> revision = getUnixFSRevisionDataStore().getCurrentRevision();
        final UnixFSJournalMutableEntry entry = getUnixFSTransactionJournal().newMutableEntry(nodeId, false);
        return new UnixFSReadWriteTransaction(nodeId, revision, entry);
    }

    @Override
    public ExclusiveReadWriteTransaction openExclusiveRW(final NodeId nodeId) {
        final UnixFSRevision<?> revision = getUnixFSRevisionDataStore().getCurrentRevision();
        final UnixFSJournalMutableEntry entry = getUnixFSTransactionJournal().newMutableEntry(nodeId, true);
        return new UnixFSExclusiveReadWriteTransaction(nodeId, revision, entry);
    }

    @Override
    public void close() {
        try {
            getUnixFSRevisionDataStore().close();
        } catch (Exception ex) {
            logger.error("Caught exception closing {}", getClass().getName(), ex);
        }
    }

    public UnixFSRevisionPool getUnixFSRevisionPool() {
        return unixFSRevisionPool;
    }

    public UnixFSRevisionDataStore getUnixFSRevisionDataStore() {
        return unixFSRevisionDataStore;
    }

    public UnixFSTransactionJournal getUnixFSTransactionJournal() {
        return unixFSTransactionJournal;
    }

    private boolean existsAt(final Revision<?> revision,
                             final ResourceId resourceId) {

        final Revision<Boolean> exists = getUnixFSRevisionDataStore()
            .getResourceIndex()
            .existsAt(revision.comparableTo(), resourceId);

        return exists.getValue().isPresent() && exists.getValue().get();

    }

    private Stream<ResourceService.Listing> listAt(final NodeId nodeId, final Revision<?> revision, final Path path) {

        final NodeId resolvedNodeId = path
            .getOptionalNodeId()
            .orElse(nodeId);

        final Revision<Stream<ResourceService.Listing>> listingRevision = getUnixFSRevisionDataStore()
            .getPathIndex()
            .list(resolvedNodeId, revision.comparableTo(), path);

        return listingRevision.getValue().orElseGet(Stream::empty);

    }

    private ResourceId getResourceIdAt(final NodeId nodeId, final Revision<?> revision, final Path path) {

        final NodeId resolvedNodeId = path
            .getOptionalNodeId()
            .orElse(nodeId);

        return getUnixFSRevisionDataStore()
            .getPathIndex()
            .getRevisionMap(resolvedNodeId)
            .getValueAt(revision.comparableTo(), path)
            .getValue()
            .orElseThrow(ResourceNotFoundException::new);

    }

    private ReadableByteChannel loadResourceContentsAt(final Revision<?> revision,
                                                       final ResourceId resourceId) throws IOException {

        final Revision<ReadableByteChannel> readableByteChannelRevision = getUnixFSRevisionDataStore()
            .getResourceIndex()
            .loadResourceContentsAt(revision.comparableTo(), resourceId);

        return readableByteChannelRevision.getValue().orElseThrow(() -> new ResourceNotFoundException());

    }

    private class UnixFSReadOnlyTransaction implements ReadOnlyTransaction {

        private final NodeId nodeId;

        private final UnixFSRevision<?> revision;

        private final TransactionJournal.Entry entry;

        public UnixFSReadOnlyTransaction(final NodeId nodeId,
                                         final UnixFSRevision revision,
                                         final TransactionJournal.Entry entry) {
            this.entry = entry;
            this.revision = revision;
            this.nodeId = nodeId;
        }

        @Override
        public Revision<?> getReadRevision() {
            return revision;
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            return existsAt(revision, resourceId);
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {
            return listAt(nodeId, revision, path);
        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return getResourceIdAt(nodeId, revision, path);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {
            return loadResourceContentsAt(revision, resourceId);
        }

        @Override
        public void close() { entry.close(); }

    }

    private class UnixFSReadWriteTransaction implements ReadWriteTransaction {

        protected final NodeId nodeId;

        private final UnixFSRevision<?> revision;

        protected final UnixFSJournalMutableEntry entry;

        public UnixFSReadWriteTransaction(final NodeId nodeId,
                                          final UnixFSRevision<?> revision,
                                          final UnixFSJournalMutableEntry entry) {
            this.entry = entry;
            this.revision = revision;
            this.nodeId = nodeId;
        }

        @Override
        public Revision<?> getReadRevision() {
            return revision;
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            return existsAt(revision, resourceId);
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {

            final Revision<Stream<ResourceService.Listing>> indexed = getUnixFSRevisionDataStore()
                .getPathIndex()
                .list(nodeId, revision, path);

            return indexed.getValue().orElseGet(Stream::empty);

        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return getResourceIdAt(nodeId, revision, path);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId)
                throws IOException {
            return loadResourceContentsAt(revision, resourceId);
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
            final Revision<?> revision = getUnixFSRevisionPool().createNextRevision();
            entry.commit(revision);
        }

        @Override
        public void close() {
            finish(nodeId, entry);
        }

    }

    private class UnixFSExclusiveReadWriteTransaction extends UnixFSReadWriteTransaction
                                                      implements ExclusiveReadWriteTransaction {

        public UnixFSExclusiveReadWriteTransaction(final NodeId nodeId,
                                                   final UnixFSRevision<?> revision,
                                                   final UnixFSJournalMutableEntry entry) {
            super(nodeId, revision, entry);
        }

        @Override
        public Stream<ResourceId> removeAllResources() {
            return unixFSTransactionJournal.clear();
        }

    }

    private void finish(final NodeId nodeId, final UnixFSJournalMutableEntry entry) {

        ExecutionHandler handler = null;

        try {

            final UnixFSRevision<?> revision = entry.getWriteRevision().getOriginal(UnixFSRevision.class);

            handler = getUnixFSRevisionDataStore().newExecutionHandler(nodeId, revision);

            if (entry.isCommitted()) {
                entry.apply(handler);
                getUnixFSRevisionDataStore().updateRevision(revision);
            } else {
                entry.rollback();
            }

        } finally {
            if (handler != null) entry.cleanup(handler);
        }

    }

}
