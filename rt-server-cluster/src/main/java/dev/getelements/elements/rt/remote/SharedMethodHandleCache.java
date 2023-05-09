package dev.getelements.elements.rt.remote;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.invoke.MethodHandle;

/**
 * Caches {@link MethodHandle} instances
 */
class SharedMethodHandleCache {

    private SharedMethodHandleCache() {}

    private static final Cache<MethodHandleKey, MethodHandle> sharedMethodHandleCache = CacheBuilder
        .newBuilder()
        .weakKeys()
        .build();

    /**
     * Retruns the singleton shared {@link Cache} to load {@link MethodHandle} instances.
     *
     * @return the singleton {@link Cache}
     */
    public static Cache<MethodHandleKey, MethodHandle> getSharedMethodHandleCache() {
        return sharedMethodHandleCache;
    }

}
