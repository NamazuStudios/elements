package com.namazustudios.socialengine.rt.xodus;
import com.namazustudios.socialengine.rt.PersistenceEnvironment;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.*;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class XodusTransactionalResourceServicePersistenceEnvironment implements
        PersistenceEnvironment,
        TransactionalResourceServicePersistence {

    private static final Logger logger = LoggerFactory.getLogger(XodusTransactionalResourceServicePersistenceEnvironment.class);

    public static final String RESOURCE_ENVIRONMENT = "com.namazustudios.socialengine.rt.xodus.resource";

    public static final String RESOURCE_ENVIRONMENT_PATH = "com.namazustudios.socialengine.rt.xodus.environment.path";

    public static final String RESOURCE_BLOCK_SIZE = "com.namazustudios.socialengine.rt.xodus.resource.block.size";

    public static final long DEFAULT_RESOURCE_BLOCK_SIZE = 4096L;

    private Provider<Environment> environmentProvider;

    private PessimisticLockingMaster pessimisticLockingMaster;

    private final AtomicReference<EnvironmentContext> environment = new AtomicReference<>();

    @Override
    public void start() {

        final var environment = new EnvironmentContext();


        if (this.environment.compareAndSet(null, environment)) {
            logger.info("Started Environment");
            setup(environment);
        } else {
            environment.close();
            throw new IllegalStateException("Environment already running.");
        }

    }

    private void setup(final EnvironmentContext environmentContext) {
        environmentContext.environment.executeInExclusiveTransaction(XodusResourceStores::create);
    }

    @Override
    public void stop() {

        final var environment = this.environment.getAndSet(null);

        if (environment == null) {
            throw new IllegalStateException("Persistence not running.");
        } else {
            environment.close();
            logger.info("Stopped Environment.");
        }

    }

    @Override
    public ReadOnlyTransaction openRO(final NodeId nodeId) {

        final var environmentContext = getEnvironmentContext();
        final var environment = environmentContext.environment;

        return open(environment::beginReadonlyTransaction, txn -> {
            final var stores = new XodusResourceStores(txn, environmentContext.environment);
            return new XodusReadOnlyTransaction(nodeId, stores, environmentContext.virtualFileSystem, txn);
        });

    }

    @Override
    public ReadWriteTransaction openRW(final NodeId nodeId) {

        final var environment = getEnvironmentContext().environment;

        return open(environment::beginTransaction, txn -> {
            final var stores = new XodusResourceStores(txn, getEnvironmentContext().environment);
            return new XodusReadWriteTransaction(nodeId, stores, getEnvironmentContext().virtualFileSystem, txn);
        });

    }
    @Override
    public ExclusiveReadWriteTransaction openExclusiveRW(NodeId nodeId) {

        final var environment = getEnvironmentContext().environment;

        return open(environment::beginExclusiveTransaction, txn -> {
            final var stores = new XodusResourceStores(txn, getEnvironmentContext().environment);
            return new XodusExclusiveReadWriteTransaction(nodeId, stores, getEnvironmentContext().virtualFileSystem, txn);
        });

    }
    private <T extends ReadOnlyTransaction> T open(final Supplier<Transaction> xodusTransactionSupplier,
                                                   final Function<Transaction, T> persistenceTransactionSupplier) {
        final var txn = xodusTransactionSupplier.get();
        return persistenceTransactionSupplier.apply(txn);
    }

    private EnvironmentContext getEnvironmentContext() {

        final var environmentContext = this.environment.get();

        if (environmentContext == null) throw new IllegalStateException("Persistence not running.");

        return environmentContext;
    }

    public Provider<Environment> getEnvironmentProvider() {
        return environmentProvider;
    }

    @Inject
    public void setEnvironmentProvider(@Named(RESOURCE_ENVIRONMENT) Provider<Environment> environmentProvider) {
        this.environmentProvider = environmentProvider;
    }

    private class EnvironmentContext implements AutoCloseable {

        private final Environment environment = getEnvironmentProvider().get();

        private final VirtualFileSystem virtualFileSystem = new VirtualFileSystem(environment);

        @Override
        public void close() {
            virtualFileSystem.shutdown();
            environment.close();
        }

    }

}
