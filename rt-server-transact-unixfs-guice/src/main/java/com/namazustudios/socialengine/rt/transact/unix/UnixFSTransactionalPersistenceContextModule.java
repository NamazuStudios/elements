package com.namazustudios.socialengine.rt.transact.unix;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionDataStore;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import com.namazustudios.socialengine.rt.transact.TransactionalPersistenceContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSChecksumAlgorithm.ADLER_32;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionPool.REVISION_POOL_SIZE;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTable.REVISION_TABLE_COUNT;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionJournal.TRANSACTION_BUFFER_COUNT;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionJournal.TRANSACTION_BUFFER_SIZE;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.STORAGE_ROOT_DIRECTORY;
import static java.lang.String.format;

public class UnixFSTransactionalPersistenceContextModule extends PrivateModule {

    private Runnable storageRootBinding = () -> {};

    private Runnable transactionSizeBinding = () -> {};

    private Runnable transactionCountBinding = () -> {};

    private Runnable revisionTableCountBinding = () -> {};

    private Runnable revisionPoolSizeBinding = () -> {};

    private Runnable checksumAlgorithmBinding = () -> {};

    private Runnable exposeDetailsForTesting = () -> {};

    @Override
    protected void configure() {

        bind(UnixFSUtils.class).asEagerSingleton();
        bind(UnixFSGarbageCollector.class).asEagerSingleton();
        bind(UnixFSRevisionPool.class).asEagerSingleton();
        bind(UnixFSRevisionTable.class).asEagerSingleton();
        bind(UnixFSTransactionJournal.class).asEagerSingleton();
        bind(UnixFSTransactionalPersistenceContext.class).asEagerSingleton();

        bind(RevisionDataStore.class).to(UnixFSRevisionDataStore.class).asEagerSingleton();
        bind(Revision.Factory.class).to(UnixFSRevisionPool.class).asEagerSingleton();
        bind(TransactionJournal.class).to(UnixFSTransactionJournal.class).asEagerSingleton();
        bind(TransactionalPersistenceContext.class).to(UnixFSTransactionalPersistenceContext.class).asEagerSingleton();

        storageRootBinding.run();
        transactionSizeBinding.run();
        transactionCountBinding.run();
        revisionTableCountBinding.run();
        revisionPoolSizeBinding.run();
        checksumAlgorithmBinding.run();
        exposeDetailsForTesting.run();

        expose(RevisionDataStore.class);
        expose(TransactionJournal.class);
        expose(TransactionalPersistenceContext.class);

    }

    /**
     * Specifies the storage root directory.
     *
     * @param path the path
     * @return
     */
    public UnixFSTransactionalPersistenceContextModule withStorageRoot(final Path path) {
        storageRootBinding = () -> bind(Path.class).annotatedWith(named(STORAGE_ROOT_DIRECTORY)).toInstance(path);
        return this;
    }

    /**
     * Specifies the transaction buffer size. That is the number of entries the transaction buffer will contain.
     *
     * @param size the size of the transaction buffer
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule withTransactionBufferSize(final int size) {
        transactionSizeBinding = () ->
            bind(long.class)
                .annotatedWith(named(TRANSACTION_BUFFER_SIZE))
                .toInstance((long)size);
        return this;
    }

    /**
     * Specifies the transaction buffer count. That is the number of entries which may be present in the transaction
     * journal.
     *
     * @param count the count
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule withTransactionBufferCount(final int count) {
        transactionCountBinding = () ->
            bind(long.class)
                .annotatedWith(named(TRANSACTION_BUFFER_COUNT))
                .toInstance((long)count);
        return this;
    }

    /**
     * Specifies the revision table count. This is the number of active revisions that the database can have. This
     * should be sufficiently large enough to keep active transactions that do not outpace the garbage collector.
     *
     * @param count the count
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule withRevisionTableCount(final int count) {
        revisionTableCountBinding = () -> bind(int.class).annotatedWith(named(REVISION_TABLE_COUNT)).toInstance(count);
        return this;
    }

    /**
     * Specifies the size of the revision pool. This is the highest value a revision number will have before rolling
     * over, this also limits the number of concurrent transactions. However, this should not be greatly exceed the
     *
     * @param size
     * @return
     */
    public UnixFSTransactionalPersistenceContextModule withRevisionPoolSize(final int size) {
        revisionPoolSizeBinding = () -> bind(int.class).annotatedWith(named(REVISION_POOL_SIZE)).toInstance(size);
        return this;
    }

    /**
     * Specifies the {@link UnixFSChecksumAlgorithm} used for new entries in the journal and the revision table.
     *
     * @param algo the algorithm to use
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule withChecksumAlgorithm(final UnixFSChecksumAlgorithm algo) {
        checksumAlgorithmBinding = () -> bind(UnixFSChecksumAlgorithm.class).toInstance(algo);
        return this;
    }

    /**
     * Exposes the details of the {@link UnixFSTransactionalPersistenceContextModule} for testing purposes.
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule exposeDetailsForTesting() {

        exposeDetailsForTesting = () -> {
            expose(UnixFSUtils.class);
            expose(UnixFSGarbageCollector.class);
            expose(UnixFSRevisionPool.class);
            expose(UnixFSRevisionTable.class);
            expose(UnixFSTransactionJournal.class);
            expose(UnixFSTransactionalPersistenceContext.class);
        };

        return this;

    }

    /**
     * Sets up a configuration for testing. This creates a temporary directory to contain the database as well as
     * configures it.
     *
     * @return this instance
     * @throws IOException if there was an error creating the test directory
     */
    public UnixFSTransactionalPersistenceContextModule withTestingDefaults() throws IOException {
        return withTestingDefaults("");
    }

    /**
     * Sets up a configuration for testing. This creates a temporary directory to contain the database as well as
     * configures it.
     *
     * @param name the name of the test, this is useful for examining the post-run output if necessary
     * @return this instance
     * @throws IOException if there was an error creating the test directory
     */
    public UnixFSTransactionalPersistenceContextModule withTestingDefaults(final String name) throws IOException {

        final String prefix = name == null || name.trim().isEmpty() ?
            "elements-unixfs-test" :
            format("elements-unixfs-test-%s", name);

        final Path path = Files.createTempDirectory(prefix);
        return withStorageRoot(path)
               .withTransactionBufferSize(4096)
               .withTransactionBufferCount(4096)
               .withRevisionTableCount(4096)
               .withChecksumAlgorithm(ADLER_32)
               .withRevisionPoolSize(Integer.MAX_VALUE);

    }

}
