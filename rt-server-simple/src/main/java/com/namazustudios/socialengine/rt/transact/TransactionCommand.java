package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;

/**
 * Enumerates all commands that can take place during the scope of a transaction.
 */
public enum TransactionCommand {

    /**
     * Links path to a {@link ResourceId}.
     */
    LINK,

    /**
     * Unlinks a {@link Path} from a {@link ResourceId}
     */
    UNLINK,

    /**
     * Acquire a {@link Resource} based on {@link ResourceId}.
     */
    ACQUIRE,

    /**
     * Deletes a {@link ResourceId} and it associated {@link Resource}.
     */
    DELETE,

    /**
     * Deletes a {@link ResourceId} and it associated {@link Resource}.
     */
    RELEASE,

    /**
     * Commits a {@link Resource} to disk, as invoked by the yield instruction.
     */
    YIELD_COMMIT

}
