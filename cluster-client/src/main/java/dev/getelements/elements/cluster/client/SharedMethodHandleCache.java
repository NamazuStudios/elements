package dev.getelements.elements.cluster.client;

import dev.getelements.elements.sdk.cluster.remote.proxy.MethodHandleKey;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Caches {@link MethodHandle} instances
 */
public class SharedMethodHandleCache {

    private SharedMethodHandleCache() {}

    private static final Map<MethodHandleKey, MethodHandle> sharedMethodHandleCache = new WeakHashMap<>();

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Returns the cached {@link MethodHandle} for the supplied key, computing and caching it using the supplied
     * {@link Supplier} if no entry is yet present.
     *
     * Reads are expected to vastly outnumber writes, so lookups are performed under a shared read lock and the
     * exclusive write lock is only acquired to populate a missing entry.
     *
     * @param key the key
     * @param methodHandleSupplier supplies the {@link MethodHandle} if absent from the cache
     * @return the cached or newly computed {@link MethodHandle}
     */
    public static MethodHandle computeIfAbsent(
            final MethodHandleKey key,
            final Supplier<MethodHandle> methodHandleSupplier) {

        lock.readLock().lock();

        try {
            final MethodHandle existing = sharedMethodHandleCache.get(key);
            if (existing != null) {
                return existing;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();

        try {
            return sharedMethodHandleCache.computeIfAbsent(key, k -> methodHandleSupplier.get());
        } finally {
            lock.writeLock().unlock();
        }

    }

}
