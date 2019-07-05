package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.TaskId;

import java.util.function.Consumer;

/**
 * Allows for the manipulating of handler {@link Resource}s as shared instances.  Shared instances are created, one or
 * more methods invoked, and then the original path is then released.  The {@link Resource} itself may opt to index
 * itself to ensure that it is retained after this call.
 */
public interface RetainedHandlerService {

    /**
     * Creates a new {@link Resource} using the attributes and the module.  Once created, this {@link Resource} will be
     * kept in memory for any number of operations.  Once the operations is complete, the system-assigned {@link Path}
     * to the {@link Resource} will be unlinked.  If the {@link Resource} needs to persist beyond the creation of this
     * call it must provide additional indexing at some point during this process.
     *
     * @param success the {@link Consumer} to receive the successful response
     * @param failure the {@link Consumer} to receieve the failed response
     * @param module the module to use when creating the {@link Resource}
     * @param attributes the attributes to use when creating the {@link Resource}
     * @param method the method to invoke
     * @param args
     * @return the result of the operation
     */
    TaskId perform(Consumer<Object> success, Consumer<Throwable> failure,
                   String module, Attributes attributes,
                   String method, Object... args);

}
