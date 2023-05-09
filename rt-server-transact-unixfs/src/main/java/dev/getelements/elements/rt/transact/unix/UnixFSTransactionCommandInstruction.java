package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.id.ResourceId;

import java.nio.file.Files;

/**
 * Indicates the instruction.
 */
enum UnixFSTransactionCommandInstruction {

    /**
     * No-op.
     */
    NOOP,

    /**
     * Unlinks a filesystem path {@link ResourceId}, typically implented using
     * {@link Files#delete(java.nio.file.Path)} or similar
     * functionality.
     */
    UNLINK_FS_PATH,

    /**
     * Unlinks a {@link ResourceId} from a {@link dev.getelements.elements.rt.Path}.
     */
    UNLINK_RT_PATH,

    /**
     * Removes a {@link Resource} with a supplied {@link ResourceId}
     */
    REMOVE_RESOURCE,

    /**
     * Updates an existing {@link Resource to a new version. This accepts botht he resource file to update as well
     * as the {@link ResourceId}.
     */
    UPDATE_RESOURCE,

    /**
     * Adds the supplied {@link dev.getelements.elements.rt.Path} to the. This does not actually link the path
     * but rather makes the underlying datastore aware that the path exists ensuring that the preconditions for usage of
     * the path is necessary.
     */
    ADD_PATH,

    /**
     * Adds a {@link ResourceId} to the underlying data store. Unlike {@link #LINK_NEW_RESOURCE}, there does not need to
     * actually be contents of the resource to link. This simply makes the underlying datastore aware of the existence
     * of the underlying {@link ResourceId}.
     *
     */
    ADD_RESOURCE_ID,

    /**
     * Links a {@link java.nio.file.Path} to a {@link ResourceId}. Specifically, this would be used to link a
     * temporary file at the supplied {@link java.nio.file.Path} to a {@link ResourceId}. The existing resource must
     * not exist as this operation only makes sense for the first time a ResourceId is introduced.
     */
    LINK_NEW_RESOURCE,

    /**
     * Links a {@link ResourceId} to a {@link dev.getelements.elements.rt.Path}
     */
    LINK_RESOURCE_TO_RT_PATH

}
