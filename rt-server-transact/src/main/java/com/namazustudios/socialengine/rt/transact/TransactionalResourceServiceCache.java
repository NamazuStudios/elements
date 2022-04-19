package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.util.Monitor;

import java.util.stream.Stream;

public interface TransactionalResourceServiceCache {

    /**
     * Returns the in-memory count of resources. This is meant to be quick inspection of the state of the service and
     * should not be used to make critical-path data decisions. This meant mostly to satisfy reporting and benchmarking
     * requirements.
     *
     * @return the size of the cache, in terms of resource count
     */
    long size();

    /**
     * Called to acquire a read monitor. If an operation anticipates it may make a concurrent modification to any
     * resource in the cache, this should be acquired in advance of mutation operations. Failing to do this while
     * performing a critical operation may result unpredictable behavior of the cache.
     *
     * @return a {@link Monitor} which locks the cache for read-only operations
     */
    Monitor readMonitor();

    /**
     * Opens a {@link Mutator} which obtains a snapshot or locked view of this cache. Once closed the operation will be
     * applied to the cache. This can be implemented as a lock or series of locks.
     * @param resourceId the {@link ResourceId} a {@link Mutator} may operate against only one single {@link ResourceId}
     * @return a {@link Mutator} which locks the cache for read-write operations
     */
    Mutator mutate(ResourceId resourceId);

    /**
     * Obtains an exclusive mutator. Doing this ensures that only one thread may operate against this cache at a time.
     * This is really only useful for clearing the cache.
     *
     * @return the exclusive mutator
     */
    ExclusiveMutator exclusive();

    /**
     * Allows for the operation of mutations against a single resource id in the cache.
     */
    interface Mutator extends AutoCloseable {

        /**
         * True if the cache contains the resource id.
         *
         * @return true if it contains the resource id, false otherwise
         */
        boolean isPresent();

        /**
         * Removes the resource, if associated with the resource id, from the cache.
         */
        void purge();

        /**
         * Acquires and returns the resource, or throws an exception.
         *
         * @return the {@link TransactionalResource}
         */
        TransactionalResource acquire();

        /**
         * Acquires a newly loaded {@link Resource}.
         *
         * @param loaded the raw resource loaded from disk.
         * @return the acquired {@link TransactionalResource}
         */
        TransactionalResource acquire(Resource loaded);

        /**
         * Gets the associated {@link TransactionalResource}.
         *
         * @return the associated resource
         */
        TransactionalResource getResource();

        /**
         * Closes this {@link Mutator} and releases any locks.
         */
        @Override
        void close();

    }

    /**
     * Mutates the cache while obtaining an exclusive lock.
     */
    interface ExclusiveMutator extends AutoCloseable {

        /**
         * Clears all resources in the cache.
         *
         * @return the {@link Stream<Resource>}
         */
        Stream<Resource> clear();

        /**
         * Closes this {@link ExclusiveMutator} and releases any locks.
         */
        @Override
        void close();

    }

}
