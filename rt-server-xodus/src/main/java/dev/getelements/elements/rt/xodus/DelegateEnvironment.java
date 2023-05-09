package dev.getelements.elements.rt.xodus;

import jetbrains.exodus.backup.BackupStrategy;
import jetbrains.exodus.crypto.StreamCipherProvider;
import jetbrains.exodus.env.*;
import jetbrains.exodus.management.Statistics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DelegateEnvironment implements Environment {

    private final AtomicReference<Environment> delegate = new AtomicReference<>();

    public void start(final Environment delegate) {
        if (delegate == null)
            throw new IllegalArgumentException("Delegate must not be null");
        if (!this.delegate.compareAndSet(null, delegate))
            throw new IllegalStateException("Already started.");
    }

    public Environment stop() {
        final var delegate = this.delegate.getAndSet(null);
        if (delegate == null)
            throw new IllegalStateException("No started.");
        return delegate;
    }

    private Environment getDelegate() {
        final var delegate = this.delegate.get();
        if (delegate == null) throw new IllegalStateException("Not running.");
        return delegate;
    }

    @Override
    public long getCreated() {
        return getDelegate().getCreated();
    }

    @Override
    @NotNull
    public String getLocation() {
        return getDelegate().getLocation();
    }

    @Override
    @NotNull
    public Store openStore(@NotNull String name, @NotNull StoreConfig config, @NotNull Transaction transaction) {
        return getDelegate().openStore(name, config, transaction);
    }

    @Override
    @Nullable
    public Store openStore(@NotNull String name, @NotNull StoreConfig config, @NotNull Transaction transaction, boolean creationRequired) {
        return getDelegate().openStore(name, config, transaction, creationRequired);
    }

    @Override
    public void executeTransactionSafeTask(@NotNull Runnable task) {
        getDelegate().executeTransactionSafeTask(task);
    }

    @Override
    public void clear() {
        getDelegate().clear();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    @NotNull
    public List<String> getAllStoreNames(@NotNull Transaction txn) {
        return getDelegate().getAllStoreNames(txn);
    }

    @Override
    public boolean storeExists(@NotNull String storeName, @NotNull Transaction txn) {
        return getDelegate().storeExists(storeName, txn);
    }

    @Override
    public void truncateStore(@NotNull String storeName, @NotNull Transaction txn) {
        getDelegate().truncateStore(storeName, txn);
    }

    @Override
    public void removeStore(@NotNull String storeName, @NotNull Transaction txn) {
        getDelegate().removeStore(storeName, txn);
    }

    @Override
    public void gc() {
        getDelegate().gc();
    }

    @Override
    public void suspendGC() {
        getDelegate().suspendGC();
    }

    @Override
    public void resumeGC() {
        getDelegate().resumeGC();
    }

    @Override
    @NotNull
    public Transaction beginTransaction() {
        return getDelegate().beginTransaction();
    }

    @Override
    @NotNull
    public Transaction beginTransaction(Runnable beginHook) {
        return getDelegate().beginTransaction(beginHook);
    }

    @Override
    @NotNull
    public Transaction beginExclusiveTransaction() {
        return getDelegate().beginExclusiveTransaction();
    }

    @Override
    @NotNull
    public Transaction beginExclusiveTransaction(Runnable beginHook) {
        return getDelegate().beginExclusiveTransaction(beginHook);
    }

    @Override
    @NotNull
    public Transaction beginReadonlyTransaction() {
        return getDelegate().beginReadonlyTransaction();
    }

    @Override
    @NotNull
    public Transaction beginReadonlyTransaction(Runnable beginHook) {
        return getDelegate().beginReadonlyTransaction(beginHook);
    }

    @Override
    public void executeInTransaction(@NotNull TransactionalExecutable executable) {
        getDelegate().executeInTransaction(executable);
    }

    @Override
    public void executeInExclusiveTransaction(@NotNull TransactionalExecutable executable) {
        getDelegate().executeInExclusiveTransaction(executable);
    }

    @Override
    public void executeInReadonlyTransaction(@NotNull TransactionalExecutable executable) {
        getDelegate().executeInReadonlyTransaction(executable);
    }

    @Override
    public <T> T computeInTransaction(@NotNull TransactionalComputable<T> computable) {
        return getDelegate().computeInTransaction(computable);
    }

    @Override
    public <T> T computeInExclusiveTransaction(@NotNull TransactionalComputable<T> computable) {
        return getDelegate().computeInExclusiveTransaction(computable);
    }

    @Override
    public <T> T computeInReadonlyTransaction(@NotNull TransactionalComputable<T> computable) {
        return getDelegate().computeInReadonlyTransaction(computable);
    }

    @Override
    @NotNull
    public EnvironmentConfig getEnvironmentConfig() {
        return getDelegate().getEnvironmentConfig();
    }

    @Override
    @NotNull
    public Statistics getStatistics() {
        return getDelegate().getStatistics();
    }

    @Override
    @Nullable
    public StreamCipherProvider getCipherProvider() {
        return getDelegate().getCipherProvider();
    }

    @Override
    @Nullable
    public byte[] getCipherKey() {
        return getDelegate().getCipherKey();
    }

    @Override
    public long getCipherBasicIV() {
        return getDelegate().getCipherBasicIV();
    }

    @Override
    @NotNull
    public BackupStrategy getBackupStrategy() {
        return getDelegate().getBackupStrategy();
    }

}
