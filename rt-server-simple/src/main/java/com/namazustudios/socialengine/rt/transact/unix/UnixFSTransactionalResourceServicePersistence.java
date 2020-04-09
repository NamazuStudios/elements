package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.function.Consumer;
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
        return openReadOnly(entry, entry::close);
    }

    @Override
    public ReadWriteTransaction openRW() {

        final TransactionJournal.Entry entry = getJournal().newEntry();

        return new ReadWriteTransaction() {

            private ReadOnlyTransaction readOnlyTransaction = openReadOnly(entry, () -> {});

            @Override
            public Revision<?> getRevision() {
                return readOnlyTransaction.getRevision();
            }

            @Override
            public boolean exists(final ResourceId resourceId) {
                return readOnlyTransaction.exists(resourceId);
            }

            @Override
            public Stream<ResourceService.Listing> list(final Path path) {
                return readOnlyTransaction.list(path);
            }

            @Override
            public ResourceId getResourceId(Path path) {
                return null;
            }

            @Override
            public ReadableByteChannel loadResourceContents(ResourceId resourceId) throws IOException {
                return null;
            }

            @Override
            public ReadableByteChannel loadResourceContents(Path path) throws IOException {
                return null;
            }

            @Override
            public WritableByteChannel saveNewResource(Path path, ResourceId resourceId) throws IOException {
                return null;
            }

            @Override
            public void linkNewResource(Path path, ResourceId id) {

            }

            @Override
            public void linkExistingResource(ResourceId sourceResourceId, Path destination) {

            }

            @Override
            public ResourceService.Unlink unlinkPath(final Path path) {
                return null;
            }

            @Override
            public List<ResourceService.Unlink> unlinkMultiple(final Path path, final int max) {
                return null;
            }

            @Override
            public Resource removeResource(final ResourceId resourceId) {
                return null;
            }

            @Override
            public List<ResourceId> removeResources(final Path path, final int max, final Consumer<Resource> removed) {
                return null;
            }

            @Override
            public Stream<Resource> removeAllResources() {
                return null;
            }

            @Override
            public void commit() {

            }

            @Override
            public void close() {

            }

        };
    }

    private ReadOnlyTransaction openReadOnly(final TransactionJournal.Entry entry,
                                             final Runnable onClose) {
        return new ReadOnlyTransaction() {

            @Override
            public Revision<?> getRevision() {
                return entry.getRevision();
            }

            @Override
            public boolean exists(final ResourceId resourceId) {
                final Revision<Boolean> journaled = entry.exists(resourceId);
                return journaled.getOptionalValue().orElseGet(() -> existsAt(entry.getRevision(), resourceId));
            }

            @Override
            public Stream<ResourceService.Listing> list(final Path path) {

                final Revision<Stream<ResourceService.Listing>> journaled = entry.list(path);

                final Revision<Stream<ResourceService.Listing>> indexed = getRevisionDataStore()
                        .getPathIndex()
                        .list(entry.getRevision().comparableTo(), path);

                return concat(journaled.getValue(), indexed.getValue());

            }

            @Override
            public ResourceId getResourceId(Path path) {
                return null;
            }

            @Override
            public ReadableByteChannel loadResourceContents(ResourceId resourceId) throws IOException {
                return null;
            }

            @Override
            public ReadableByteChannel loadResourceContents(Path path) throws IOException {
                return null;
            }

            @Override
            public void close() {
                onClose.run();
            }

        };
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
        return exists.getOptionalValue().isPresent() && exists.getOptionalValue().get();
    }

    private Revision<Stream<ResourceService.Listing>> listAts(final Revision<Void> revision, final Path path) {
        return revisionDataStore.getPathIndex().list(revision.comparableTo(), path);
    }


}
