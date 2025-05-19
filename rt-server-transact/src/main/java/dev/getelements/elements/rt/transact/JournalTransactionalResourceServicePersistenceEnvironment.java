package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.exception.DuplicateException;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.exception.NoSuchTaskException;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.sdk.util.FinallyAction;
import org.slf4j.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class JournalTransactionalResourceServicePersistenceEnvironment implements
        PersistenceEnvironment,
        TransactionalResourceServicePersistence {

    private static final Logger logger = getLogger(JournalTransactionalResourceServicePersistenceEnvironment.class);

    private final int maxReads;

    private final Semaphore semaphore;

    private final Provider<Snapshot.Builder> snapshotBuilderProvider;

    private final DataStore dataStore;

    private final TransactionJournal transactionJournal;

    private final JournalTransactionalPersistenceDriver journalTransactionalPersistenceDriver;

    @Inject
    public JournalTransactionalResourceServicePersistenceEnvironment(
            final Provider<Snapshot.Builder> snapshotBuilderProvider,
            final DataStore dataStore,
            final TransactionJournal transactionJournal,
            final JournalTransactionalPersistenceDriver journalTransactionalPersistenceDriver) {

        this.snapshotBuilderProvider = snapshotBuilderProvider;
        this.dataStore = dataStore;
        this.transactionJournal = transactionJournal;
        this.journalTransactionalPersistenceDriver = journalTransactionalPersistenceDriver;

        // TODO: The read value should come from configuration
        this.maxReads = Integer.MAX_VALUE;
        this.semaphore = new Semaphore(maxReads, true);

    }

    @Override
    public void start() {
        getJournalTransactionalPersistenceDriver().start();
    }

    @Override
    public void stop() {
        try {
            getJournalTransactionalPersistenceDriver().stop();
        } catch (Exception ex) {
            logger.error("Caught exception closing {}", getJournalTransactionalPersistenceDriver(), ex);
        }
    }

    @Override
    public ReadOnlyTransaction.Builder<ReadOnlyTransaction> buildRO(final NodeId nodeId) {
        return new AbstractTransactionBuilder<>() {
            @Override
            public ReadOnlyTransaction begin() {
                try {
                    semaphore.acquire();
                    final var snapshot = snapshotBuilder.buildRO();
                    return new SimpleReadOnlyTransaction(nodeId, snapshot);
                } catch (InterruptedException e) {
                    throw new InternalException(e);
                }
            }
        };
    }

    @Override
    public ReadOnlyTransaction.Builder<ReadWriteTransaction> buildRW(final NodeId nodeId) {
        return new AbstractTransactionBuilder<>() {
            @Override
            public ReadWriteTransaction begin() {
                try {
                    semaphore.acquire();
                    final var journalEntry = getTransactionJournal().newMutableEntry(nodeId);
                    final var snapshot = snapshotBuilder.buildRW();
                    return new SimpleReadWriteTransaction(nodeId, snapshot, journalEntry);
                } catch (InterruptedException e) {
                    throw new InternalException(e);
                }
            }
        };
    }

    @Override
    public ExclusiveReadWriteTransaction openExclusiveRW() {
        try {
            semaphore.acquire(maxReads);
            return new SimpleExclusiveReadWriteTransaction();
        } catch (InterruptedException ex) {
            throw new InternalException(ex);
        }
    }

    public Provider<Snapshot.Builder> getSnapshotBuilderProvider() {
        return snapshotBuilderProvider;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public TransactionJournal getTransactionJournal() {
        return transactionJournal;
    }

    public JournalTransactionalPersistenceDriver getJournalTransactionalPersistenceDriver() {
        return journalTransactionalPersistenceDriver;
    }

    private abstract class AbstractTransactionBuilder<TransactionT extends ReadOnlyTransaction>
            implements ReadOnlyTransaction.Builder<TransactionT> {

        protected final Snapshot.Builder snapshotBuilder = getSnapshotBuilderProvider().get();

        @Override
        public ReadOnlyTransaction.Builder<TransactionT> with(final Path path) {
            snapshotBuilder.load(path);
            return this;
        }

        @Override
        public ReadOnlyTransaction.Builder<TransactionT> with(final ResourceId resourceId) {
            snapshotBuilder.load(resourceId);
            return this;
        }

    }

    private class SimpleReadOnlyTransaction implements ReadOnlyTransaction {

        private FinallyAction onClose = FinallyAction.begin(logger);

        private final NodeId nodeId;

        private final Snapshot snapshot;

        public SimpleReadOnlyTransaction(final NodeId nodeId, final Snapshot snapshot) {
            this.nodeId = nodeId;
            this.snapshot = snapshot;
            this.onClose = onClose
                    .then(snapshot::close)
                    .then(semaphore::release);
        }

        @Override
        public NodeId getNodeId() {
            return nodeId;
        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            check(resourceId);
            return snapshot.findResourceEntry(resourceId).isPresent();
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {
            check(path);
            final var stream = snapshot.list(path);
            onClose = onClose.then(stream::close);
            return stream;
        }

        @Override
        public ResourceId getResourceId(final Path path) {
            check(path);
            return snapshot
                    .findResourceEntry(path)
                    .flatMap(ResourceEntry::findResourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("No resource at :" + path));
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {
            check(resourceId);
            return snapshot
                    .getResourceEntry(resourceId)
                    .loadResourceContents();
        }

        @Override
        public void close() {
            onClose.close();
        }

    }

    private class SimpleReadWriteTransaction implements ReadWriteTransaction {

        private FinallyAction onClose = FinallyAction.begin(logger);

        private final NodeId nodeId;

        private final Snapshot snapshot;

        private final TransactionJournal.MutableEntry journalEntry;

        public SimpleReadWriteTransaction(final NodeId nodeId,
                                          final Snapshot snapshot,
                                          final TransactionJournal.MutableEntry journalEntry) {
            this.nodeId = nodeId;
            this.snapshot = snapshot;
            this.journalEntry = journalEntry;
            this.onClose = onClose
                    .then(journalEntry::close)
                    .then(snapshot::close)
                    .then(semaphore::release);
        }

        @Override
        public NodeId getNodeId() {
            return nodeId;
        }

        @Override
        public void deleteTask(final TaskId taskId) {

            check(taskId);

            if (snapshot.findResourceEntry(taskId.getResourceId()).isEmpty()) {
                throw new ResourceNotFoundException("Resource with id not found: " + taskId.getResourceId());
            }

            final var taskEntry = snapshot
                    .findTaskEntry(taskId.getResourceId())
                    .orElseThrow(() -> new NoSuchTaskException(taskId));

            if (!taskEntry.deleteTask(taskId)) {
                throw new NoSuchTaskException(taskId);
            }

        }

        @Override
        public void createTask(final TaskId taskId, final long timestamp) {
            check(taskId);

            if (snapshot.findResourceEntry(taskId.getResourceId()).isEmpty()) {
                throw new ResourceNotFoundException("Resource with id not found: " + taskId.getResourceId());
            }

            snapshot.getOrCreateTaskEntry(taskId.getResourceId()).addTask(taskId, timestamp);

        }

        @Override
        public boolean exists(final ResourceId resourceId) {
            check(resourceId);
            return snapshot.findResourceEntry(resourceId).isPresent();
        }

        @Override
        public Stream<ResourceService.Listing> list(final Path path) {
            check(path);
            final var stream = snapshot.list(path);
            onClose = onClose.then(stream::close);
            return stream;
        }

        @Override
        public ResourceId getResourceId(final Path path) {
            check(path);
            return snapshot
                    .getResourceEntry(path)
                    .findOriginalResourceId()
                    .orElseThrow(() -> new ResourceNotFoundException("No resource at :" + path));
        }

        @Override
        public ReadableByteChannel loadResourceContents(final ResourceId resourceId) throws IOException {
            check(resourceId);
            return snapshot
                    .getResourceEntry(resourceId)
                    .loadResourceContents();
        }

        @Override
        public WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId) throws IOException {

            check(path);

            final var pathSnapshotEntry = snapshot.findResourceEntry(path);
            final var resourceIdSnapshotEntry = snapshot.findResourceEntry(resourceId);

            if (pathSnapshotEntry.isPresent()) {

                final var msg = format("Resource already exists at path: %s -> %s",
                        path,
                        pathSnapshotEntry.get().getOriginalResourceId()
                );

                throw new DuplicateException(msg);

            }

            if (resourceIdSnapshotEntry.isPresent()) {
                throw new DuplicateException("Resource already exists: " + resourceId);
            }

            final var newResourceEntry = snapshot.add(resourceId);
            newResourceEntry.link(path);

            final var wbc = newResourceEntry
                    .updateResourceContents()
                    .write(journalEntry.getTransactionId());

            onClose = onClose.thenClose(wbc);

            return wbc;

        }

        @Override
        public WritableByteChannel updateResource(final ResourceId resourceId) throws IOException {

            check(resourceId);

            final var resourceIdSnapshotEntry = snapshot.getResourceEntry(resourceId);

            if (resourceIdSnapshotEntry.isAbsent()) {
                throw new ResourceNotFoundException("Resource not found: " + resourceId);
            }

            final var wbc = resourceIdSnapshotEntry
                    .updateResourceContents()
                    .write(journalEntry.getTransactionId());

            onClose = onClose.thenClose(wbc);

            return wbc;

        }

        @Override
        public void linkNewResource(final ResourceId resourceId, final Path path) {

            check(path);
            check(resourceId);

            final var pathSnapshotEntry = snapshot.findResourceEntry(path);
            final var resourceIdSnapshotEntry = snapshot.findResourceEntry(resourceId);

            if (pathSnapshotEntry.isPresent()) {

                final var msg = format("Resource already exists at path: %s -> %s",
                        path,
                        pathSnapshotEntry.get().getOriginalResourceId()
                );

                throw new DuplicateException(msg);

            }

            if (resourceIdSnapshotEntry.isPresent()) {
                throw new DuplicateException("Resource already added: " + resourceId);
            }

            snapshot.add(resourceId).link(path);

        }

        @Override
        public void linkExistingResource(final ResourceId sourceResourceId, final Path destination) {

            check(destination);
            check(sourceResourceId);

            if (destination.isWildcard()) {
                throw new IllegalArgumentException("Path must not be wildcard.");
            }

            snapshot.findResourceEntry(destination).ifPresent(entry -> {

                final var msg = format("Resource already exists at path: %s -> %s",
                        destination,
                        entry.getOriginalResourceId()
                );

                throw new DuplicateException(msg);

            });

            snapshot.findResourceEntry(sourceResourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + sourceResourceId) )
                    .link(destination);

        }

        @Override
        public ResourceService.Unlink unlinkPath(final Path path) {

            if (path.isWildcard()) {
                throw new IllegalArgumentException("Path must not be wildcard.");
            }

            check(path);

            final var pathSnapshotEntry = snapshot
                    .findResourceEntry(path)
                    .orElseThrow(() -> new ResourceNotFoundException("No resource at path: " + path));

            pathSnapshotEntry.unlink(path);

            final var reverse = pathSnapshotEntry.getReversePathsImmutable();
            final var removed = reverse.isEmpty();

            final var originalResourceId = pathSnapshotEntry.getOriginalResourceId();

            if (removed) {
                pathSnapshotEntry.delete();
                snapshot.findTaskEntry(originalResourceId)
                        .ifPresent(TaskEntry::delete);
            }

            return SimpleResourceServiceUnlink.from(originalResourceId, removed);

        }

        @Override
        public void removeResource(final ResourceId resourceId) {

            check(resourceId);

            final var resourceEntry = snapshot
                    .findResourceEntry(resourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("No resource found with id: " + resourceId));

            snapshot.findTaskEntry(resourceId).ifPresent(TaskEntry::delete);
            resourceEntry.delete();

        }

        @Override
        public List<ResourceId> removeResources(final Path path, final int max) {

            check(path);

            return snapshot
                    .list(path)
                    .limit(max)
                    .collect(toList())
                    .stream()
                    .map(listing -> {
                        final var entry = snapshot.getResourceEntry(listing.getResourceId());
                        final var resourceId = listing.getResourceId();
                        entry.delete();
                        snapshot.findTaskEntry(resourceId).ifPresent(TaskEntry::delete);
                        return resourceId;
                    })
                    .collect(toList());

        }

        @Override
        public void rollback() {
            journalEntry.rollback();
        }

        @Override
        public void commit() {
            flush();
            journalEntry.commit();
        }

        private void flush() {

            snapshot.getTaskEntries()
                    .stream()
                    .filter(not(NullTaskEntry::isNull))
                    .forEach(entry -> entry.flush(journalEntry));

            snapshot.getResourceEntries()
                    .stream()
                    .filter(not(NullResourceEntry::isNull))
                    .forEach(entry -> entry.flush(journalEntry));

        }

        @Override
        public void close() {
            onClose.close();
        }

    }

    private class SimpleExclusiveReadWriteTransaction implements ExclusiveReadWriteTransaction {

        @Override
        public void performOperation(final Consumer<DataStore> operation) {
            operation.accept(dataStore);
        }

        @Override
        public <T> T computeOperation(final Function<DataStore, T> operation) {
            return operation.apply(dataStore);
        }

        @Override
        public void close() {
            semaphore.release(maxReads);
        }

    }

}
