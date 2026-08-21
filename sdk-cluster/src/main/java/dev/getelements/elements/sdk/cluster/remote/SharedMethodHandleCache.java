package dev.getelements.elements.sdk.cluster.remote;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Caches {@link MethodHandle} instances
 */
class SharedMethodHandleCache {

    private SharedMethodHandleCache() {}

    private static final Map<MethodHandleKey, MethodHandle> sharedMethodHandleCache =
        Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Returns the singleton shared {@link Map} to load {@link MethodHandle} instances.
     *
     * @return the singleton {@link Map}
     */
    public static Map<MethodHandleKey, MethodHandle> getSharedMethodHandleCache() {
        return sharedMethodHandleCache;
    }

}