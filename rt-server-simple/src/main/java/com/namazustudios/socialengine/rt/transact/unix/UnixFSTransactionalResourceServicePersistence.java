package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Monitor;
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

    private final UnixFSRevisionDataStore unixFSRevisionDataStore;

    private final UnixFSTransactionJournal unixFSTransactionJournal;

    @Inject
    public UnixFSTransactionalResourceServicePersistence(final UnixFSRevisionDataStore unixFSRevisionDataStore,
                                                         final UnixFSTransactionJournal unixFSTransactionJournal) {
        this.unixFSRevisionDataStore = unixFSRevisionDataStore;
        this.unixFSTransactionJournal = unixFSTransactionJournal;
    }

    @Override
    public ReadOnlyTransaction openRO(final NodeId nodeId) {
        final UnixFSJournalEntry entry = getUnixFSTransactionJournal().newSnapshotEntry(nodeId);
        return new UnixFSReadOnlyTransaction(nodeId, entry);
    }

    @Override
    public ReadWriteTransaction openRW(final NodeId nodeId) {
        final UnixFSJournalMutableEntry entry = getUnixFSTransactionJournal().newMutableEntry(nodeId);
        return new UnixFSReadWriteTransaction(nodeId, entry);
    }

    @Override
    public ExclusiveReadWriteTransaction openExclusiveRW(final NodeId nodeId) {
        final Monitor monitor = getUnixFSTransactionJournal().getExclusiveMonitor();
        final UnixFSJournalMutableEntry entry = getUnixFSTransactionJournal().newMutableEntry(nodeId);
        return new UnixFSExclusiveReadWriteTransaction(nodeId, entry, monitor);
    }

    @Override
    public void close() {
        try {
            getUnixFSRevisionDataStore().close();
        } catch (Exception ex) {
            logger.error("Caught exception closing {}", getClass().getName(), ex);
        }
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

//    private ReadableByteChannel loadResourceContentsAt(final NodeId nodeId,
//                                                       final Revision<?> revision,
//                                                       final Path path) throws IOException {
//
//        final Revision<ReadableByteChannel> readableByteChannelRevision = getUnixFSRevisionDataStore()
//            .getResourceIndex()
//            .loadResourceContentsAt(nodeId, revision.comparableTo(), path);
//
//        return readableByteChannelRevision.getValue().orElseThrow(() -> new ResourceNotFoundException());
//
//    }

    private ReadableByteChannel loadResourceContentsAt(final Revision<?> revision,
                                                       final ResourceId resourceId) throws IOException {

        final Revision<ReadableByteChannel> readableByteChannelRevision = getUnixFSRevisionDataStore()
                .getResourceIndex()
                .loadResourceContentsAt(revision.comparableTo(), resourceId);

        return readableByteChannelRevision.getValue().orElseThrow(() -> new ResourceNotFoundException());

    }

    private class UnixFSReadOnlyTransaction implements ReadOnlyTransaction {

        private final NodeId nodeId;

        private final TransactionJournal.Entry entry;

        public UnixFSReadOnlyTransaction(final NodeId nodeId, final TransactionJournal.Entry entry) {
            this.entry = entry;
            this.nodeId = nodeId;
        }

        @Override
        public Revision<?> getRevision() {
            return entry.getRevision();
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            return existsAt(entry.getRevision(), resourceId);
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {
            return listAt(nodeId, entry.getRevision(), path);
        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return getResourceIdAt(nodeId, entry.getRevision(), path);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {
            return loadResourceContentsAt(entry.getRevision(), resourceId);
        }

        @Override
        public void close() {
            entry.close();
        }

    }

    private class UnixFSReadWriteTransaction implements ReadWriteTransaction {

        protected final NodeId nodeId;

        protected final UnixFSJournalMutableEntry entry;

        public UnixFSReadWriteTransaction(final NodeId nodeId, final UnixFSJournalMutableEntry entry) {
            this.entry = entry;
            this.nodeId = nodeId;
        }

        @Override
        public Revision<?> getRevision() {
            return entry.getRevision();
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            return existsAt(entry.getRevision(), resourceId);
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {

            final Revision<Stream<ResourceService.Listing>> indexed = getUnixFSRevisionDataStore()
                .getPathIndex()
                .list(nodeId, entry.getRevision(), path);

            return indexed.getValue().orElseGet(Stream::empty);

        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return getResourceIdAt(nodeId, entry.getRevision(), path);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId)
                throws IOException {
            return loadResourceContentsAt(entry.getRevision(), resourceId);
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
            entry.commit();
        }

        @Override
        public void close() {
            try (final TransactionJournal.Entry e = entry) {
                finish(entry);
            }
        }

    }

    private class UnixFSExclusiveReadWriteTransaction extends UnixFSReadWriteTransaction
                                                      implements ExclusiveReadWriteTransaction {

        protected final Monitor monitor;

        public UnixFSExclusiveReadWriteTransaction(final NodeId nodeId,
                                                   final UnixFSJournalMutableEntry entry,
                                                   final Monitor monitor) {
            super(nodeId, entry);
            this.monitor = monitor;
        }

        @Override
        public Stream<ResourceId> removeAllResources() {
            return unixFSTransactionJournal.clear();
        }

        @Override
        public void close() {
            try (final Monitor m = monitor;
                 final TransactionJournal.Entry e = entry) {
                finish(entry);
             }
        }

    }

    private void finish(final UnixFSJournalMutableEntry entry) {

        final ExecutionHandler handler = getUnixFSRevisionDataStore().newExecutionHandler(entry.getRevision());

        try {
            if (entry.isCommitted()) {
                entry.apply(handler);
            } else {
                entry.rollback();
            }
        } finally {
            entry.cleanup(handler);
        }

    }

}
