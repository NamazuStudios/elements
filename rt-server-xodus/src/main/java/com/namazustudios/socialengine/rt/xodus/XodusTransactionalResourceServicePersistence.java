package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.Persistence;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.*;
import jdk.jfr.Name;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.xodus.XodusResourceStores.create;

public class XodusTransactionalResourceServicePersistence implements Persistence, TransactionalResourceServicePersistence {

    private static final Logger logger = LoggerFactory.getLogger(XodusTransactionalResourceServicePersistence.class);

    public static final String RESOURCE_ENVIRONMENT = "com.namazustudios.socialengine.rt.xodus.resource";

    public static final String RESOURCE_BLOCK_SIZE = "com.namazustudios.socialengine.rt.xodus.resource.block.size";

    public static final long DEFAULT_RESOURCE_BLOCK_SIZE = 4096L;

    private long blockSize;

    private Provider<Environment> environmentProvider;

    private PessimisticLockingMaster pessimisticLockingMaster;

    private final AtomicReference<Environment> environment = new AtomicReference<>();

    @Override
    public void start() {

        final var environment = getEnvironmentProvider().get();

        if (this.environment.compareAndSet(null, environment)) {
            logger.info("Started Environment");
            setup(environment);
        } else {
            environment.close();
            throw new IllegalStateException("Environment already running.");
        }

    }

    private void setup(final Environment environment) {
        environment.executeInExclusiveTransaction(XodusResourceStores::create);
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
        final var environment = getEnvironment();
        return open(environment::beginReadonlyTransaction, txn -> {
            final var stores = new XodusResourceStores(txn, environment);
            return new XodusReadOnlyTransaction(nodeId, stores, txn);
        });
    }

    @Override
    public ReadWriteTransaction openRW(final NodeId nodeId) {
        final var environment = getEnvironment();
        return open(environment::beginTransaction, txn -> {
            final var stores = new XodusResourceStores(txn, environment);
            final var pessimisticLocking = getPessimisticLockingMaster().newPessimisticLocking();
            return new XodusReadWriteTransaction(nodeId, getBlockSize(), stores, txn, pessimisticLocking);
        });
    }

    @Override
    public ExclusiveReadWriteTransaction openExclusiveRW(NodeId nodeId) {
        final var environment = getEnvironment();
        return open(environment::beginExclusiveTransaction, txn -> {
            final var stores = new XodusResourceStores(txn, environment);
            final var pessimisticLocking = getPessimisticLockingMaster().newPessimisticLocking();
            return new XodusExclusiveReadWriteTransaction(nodeId, getBlockSize(), stores, txn, pessimisticLocking);
        });
    }

    private <T extends ReadOnlyTransaction> T open(final Supplier<Transaction> xodusTransactionSupplier,
                                                   final Function<Transaction, T> persistenceTransactionSupplier) {

        final var txn = xodusTransactionSupplier.get();

        try {
            return persistenceTransactionSupplier.apply(txn);
        } catch (Exception ex) {
            txn.abort();
            logger.error("Could not open transaction.", ex);
            throw ex;
        }

    }

    public long getBlockSize() {
        return blockSize;
    }

    private Environment getEnvironment() {
        final var environment = this.environment.get();
        if (environment == null) throw new IllegalStateException("Persistence not running.");
        return environment;
    }

    @Inject
    public void setBlockSize(@Named(RESOURCE_BLOCK_SIZE) long blockSize) {
        this.blockSize = blockSize;
    }

    public Provider<Environment> getEnvironmentProvider() {
        return environmentProvider;
    }

    @Inject
    public void setEnvironmentProvider(@Named(RESOURCE_ENVIRONMENT) Provider<Environment> environmentProvider) {
        this.environmentProvider = environmentProvider;
    }

    public PessimisticLockingMaster getPessimisticLockingMaster() {
        return pessimisticLockingMaster;
    }

    @Inject
    public void setPessimisticLockingMaster(PessimisticLockingMaster pessimisticLockingMaster) {
        this.pessimisticLockingMaster = pessimisticLockingMaster;
    }

}
