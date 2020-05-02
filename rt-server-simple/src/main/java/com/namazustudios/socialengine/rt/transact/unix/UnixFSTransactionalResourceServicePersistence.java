package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Monitor;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static org.slf4j.LoggerFactory.getLogger;

public class UnixFSTransactionalResourceServicePersistence implements TransactionalResourceServicePersistence {

    private static final Logger logger = getLogger(UnixFSTransactionalResourceServicePersistence.class);

    private final TransactionJournal journal;

    private final RevisionDataStore revisionDataStore;

    @Inject
    public UnixFSTransactionalResourceServicePersistence(final TransactionJournal journal,
                                                         final RevisionDataStore revisionDataStore) {
        this.journal = journal;
        this.revisionDataStore = revisionDataStore;
    }

    @Override
    public ReadOnlyTransaction openRO() {
        final TransactionJournal.Entry entry = getJournal().getCurrentEntry();
        return new UnixFSReadOnlyTransaction(entry);
    }

    @Override
    public ReadWriteTransaction openRW() {
        final TransactionJournal.MutableEntry entry = getJournal().newEntry();
        return new UnixFSReadWriteTransaction(entry);
    }

    @Override
    public ExclusiveReadWriteTransaction openExclusiveRW() {
        final Monitor monitor = getJournal().getExclusiveMonitor();
        final TransactionJournal.MutableEntry entry = getJournal().newEntry();
        return new UnixFSExclusiveReadWriteTransaction(entry, monitor);
    }

    @Override
    public void close() {
        try {
            getRevisionDataStore().close();
        } catch (Exception ex) {
            logger.error("Caught exception closing {}", getClass().getName(), ex);
        }
    }

    public TransactionJournal getJournal() {
        return journal;
    }

    public RevisionDataStore getRevisionDataStore() {
        return revisionDataStore;
    }

    private boolean existsAt(final Revision<?> revision, final ResourceId resourceId) {
        final Revision<Boolean> exists;
        exists = revisionDataStore.getResourceIndex().existsAt(revision.comparableTo(), resourceId);
        return exists.getValue().isPresent() && exists.getValue().get();
    }

    private Stream<ResourceService.Listing> listAt(final Revision<?> revision, final Path path) {
        final Revision<Stream<ResourceService.Listing>> listingRevision;
        listingRevision = revisionDataStore.getPathIndex().list(revision.comparableTo(), path);
        return listingRevision.getValue().orElseGet(Stream::empty);
    }

    private ResourceId getResourceIdAt(final Revision<?> revision, final Path path) {
        return revisionDataStore
            .getPathIndex()
            .getRevisionMap()
            .getValueAt(revision.comparableTo(), path)
            .getValue()
            .orElseThrow(ResourceNotFoundException::new);
    }

    private ReadableByteChannel loadResourceContentsAt(final Revision<?> revision, final Path path) {

        final Revision<ReadableByteChannel> readableByteChannelRevision = revisionDataStore
            .getResourceIndex()
            .loadResourceContentsAt(revision.comparableTo(), path);

        return readableByteChannelRevision.getValue().orElseThrow(() -> new ResourceNotFoundException());

    }

    private ReadableByteChannel loadResourceContentsAt(final Revision<?> revision, final ResourceId resourceId) {

        final Revision<ReadableByteChannel> readableByteChannelRevision = revisionDataStore
                .getResourceIndex()
                .loadResourceContentsAt(revision.comparableTo(), resourceId);

        return readableByteChannelRevision.getValue().orElseThrow(() -> new ResourceNotFoundException());

    }

    private class UnixFSReadOnlyTransaction implements ReadOnlyTransaction {

        private final TransactionJournal.Entry entry;

        public UnixFSReadOnlyTransaction(TransactionJournal.Entry entry) {
            this.entry = entry;
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
            return listAt(entry.getRevision(), path);
        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return getResourceIdAt(entry.getRevision(), path);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId) {
            return loadResourceContentsAt(entry.getRevision(), resourceId);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final Path path) {
            return loadResourceContentsAt(entry.getRevision(), path);
        }

        @Override
        public void close() {
            entry.close();
        }

    }

    private class UnixFSReadWriteTransaction implements ReadWriteTransaction {

        protected final TransactionJournal.MutableEntry entry;

        public UnixFSReadWriteTransaction(TransactionJournal.MutableEntry entry) {
            this.entry = entry;
        }

        @Override
        public Revision<?> getRevision() {
            return entry.getRevision();
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            final Revision<Boolean> journaled = entry.exists(resourceId);
            return journaled.getValue().orElseGet(() -> existsAt(entry.getRevision(), resourceId));
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {

            final Revision<Stream<ResourceService.Listing>> journaled = entry.list(path);

            final Revision<Stream<ResourceService.Listing>> indexed = getRevisionDataStore()
                    .getPathIndex()
                    .list(entry.getRevision(), path);

            return concat(
                journaled.getValue().orElseGet(Stream::empty),
                indexed.getValue().orElseGet(Stream::empty));

        }

        @Override
        public ResourceId getResourceId(final Path path) {
            return entry.getResourceId(path).getValue().orElseGet(() -> getResourceIdAt(entry.getRevision(), path));
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {
            final Revision<ReadableByteChannel> channelRevision = entry.loadResourceContents(resourceId);
            if (channelRevision.getValue().isPresent()) return channelRevision.getValue().get();
            return loadResourceContentsAt(entry.getRevision(), resourceId);
        }

        @Override
        public ReadableByteChannel loadResourceContents(final Path path) throws IOException {
            final Revision<ReadableByteChannel> channelRevision = entry.loadResourceContents(path);
            if (channelRevision.getValue().isPresent()) return channelRevision.getValue().get();
            return loadResourceContentsAt(entry.getRevision(), path);
        }

        @Override
        public WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId) throws IOException {
            return entry.saveNewResource(path, resourceId);
        }

        @Override
        public void linkNewResource(final Path path, final ResourceId id) {
            entry.linkNewResource(id, path);
        }

        @Override
        public void linkExistingResource(final ResourceId sourceResourceId, final Path destination) {
            entry.linkExistingResource(sourceResourceId, destination);
        }

        @Override
        public ResourceService.Unlink unlinkPath(final Path path) {
            return entry.unlinkPath(path);
        }

        @Override
        public List<ResourceService.Unlink> unlinkMultiple(final Path path, final int max) {
            return entry.unlinkMultiple(path, max);
        }

        @Override
        public void removeResource(final ResourceId resourceId) {
            entry.removeResource(resourceId);
        }

        @Override
        public List<ResourceId> removeResources(final Path path, final int max) {
            return entry.removeResources(path, max);
        }

        @Override
        public void commit() {
            entry.commit();
        }

        @Override
        public void close() {
            try (final TransactionJournal.Entry e = entry) {
                if (entry.isCommitted()) {
                    revisionDataStore.apply(entry);
                }
            }
        }

    }

    private class UnixFSExclusiveReadWriteTransaction extends UnixFSReadWriteTransaction
                                                      implements ExclusiveReadWriteTransaction {

        private final Monitor monitor;

        public UnixFSExclusiveReadWriteTransaction(final TransactionJournal.MutableEntry entry,
                                                   final Monitor monitor) {
            super(entry);
            this.monitor = monitor;
        }

        @Override
        public Stream<ResourceId> removeAllResources() {
            return journal.removeAllResources();
        }

        @Override
        public void close() {
            try (final Monitor m = monitor;
                 final TransactionJournal.Entry e = entry) {
                if (entry.isCommitted()) {
                    revisionDataStore.apply(entry);
                }
            }
        }

    }

}
