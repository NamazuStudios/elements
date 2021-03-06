package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.stream.Stream;

/**
 * Operates with an exclusive lock to the database. Used for bulk operations such as truncating or deleting all
 * {@link Resource}s
 */
public interface ExclusiveReadWriteTransaction extends ReadWriteTransaction {

    /**
     * Removes all {@link Resource} instances and return their {@link ResourceId}s
     * @return
     */
    Stream<ResourceId> removeAllResources();

    /**
     * Truncates all data in the datastore.
     */
    default void truncate() {
        removeAllResources();
    }

}
