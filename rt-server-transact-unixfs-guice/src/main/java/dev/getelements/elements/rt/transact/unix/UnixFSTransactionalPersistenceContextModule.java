package dev.getelements.elements.rt.transact.unix;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.transact.DataStore;
import dev.getelements.elements.rt.transact.JournalTransactionalPersistenceDriver;
import dev.getelements.elements.rt.transact.TransactionJournal;
import dev.getelements.elements.sdk.util.TemporaryFiles;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.transact.unix.UnixFSChecksumAlgorithm.ADLER_32;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionJournal.UNIXFS_TRANSACTION_BUFFER_SIZE;
import static dev.getelements.elements.rt.transact.unix.UnixFSUtils.UNIXFS_STORAGE_ROOT_DIRECTORY;
import static java.lang.String.format;

public class UnixFSTransactionalPersistenceContextModule extends PrivateModule {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(UnixFSTransactionalPersistenceContextModule.class);

    private Runnable storageRootBinding = () -> {};

    private Runnable transactionSizeBinding = () -> {};

    private Runnable checksumAlgorithmBinding = () -> {};

    private Runnable exposeDetailsForTesting = () -> {};

    @Override
    protected void configure() {

        bind(UnixFSUtils.class).asEagerSingleton();
        bind(UnixFSTransactionJournal.class).asEagerSingleton();
        bind(UnixFSJournalTransactionalPersistenceDriver.class).asEagerSingleton();

        bind(DataStore.class).to(UnixFSDataStore.class);
        bind(TransactionJournal.class).to(UnixFSTransactionJournal.class).asEagerSingleton();
        bind(JournalTransactionalPersistenceDriver.class).to(UnixFSJournalTransactionalPersistenceDriver.class).asEagerSingleton();

        storageRootBinding.run();
        transactionSizeBinding.run();
        checksumAlgorithmBinding.run();
        exposeDetailsForTesting.run();

        expose(DataStore.class);
        expose(TransactionJournal.class);
        expose(JournalTransactionalPersistenceDriver.class);

    }

    /**
     * Specifies the storage root directory.
     *
     * @param path the path
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule withStorageRoot(final Path path) {
        storageRootBinding = () -> bind(Path.class).annotatedWith(named(UNIXFS_STORAGE_ROOT_DIRECTORY)).toInstance(path);
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
            bind(int.class)
                .annotatedWith(named(UNIXFS_TRANSACTION_BUFFER_SIZE))
                .toInstance(size);
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
            expose(UnixFSTransactionJournal.class);
            expose(UnixFSJournalTransactionalPersistenceDriver.class);
        };

        return this;

    }

    /**
     * Sets up a configuration for testing. This creates a temporary directory to contain the database as well as
     * configures it.
     *
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule withTestingDefaults() {
        return withTestingDefaults("");
    }

    /**
     * Sets up a configuration for testing. This creates a temporary directory to contain the database as well as
     * configures it.
     *
     * @param name the name of the test, this is useful for examining the post-run output if necessary
     * @return this instance
     */
    public UnixFSTransactionalPersistenceContextModule withTestingDefaults(final String name) {

        final String prefix = name == null || name.trim().isEmpty() ?
            "elements-unixfs-test" :
            format("elements-unixfs-test-%s", name);

        storageRootBinding = () -> bind(Path.class)
            .annotatedWith(named(UNIXFS_STORAGE_ROOT_DIRECTORY))
            .toProvider(() -> temporaryFiles.createTempDirectory(prefix));

        return withTransactionBufferSize(4096)
            .withChecksumAlgorithm(ADLER_32);

    }

}
