package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.Resource;

/**
 * Created by patricktwohig on 8/23/15.
 */
public interface InternalResource extends Resource {

    /**
     * Used to implement a reference counting scheme.  This should atomically increment the reference
     * count and then return the result.  A freshly instantiated object must have a reference count of
     * one.
     *
     * Note, this is to be used by the {@link InternalServer} isntance and should not be invoked
     * directly.
     *
     * @return the retain count
     */
    int retain();

    /**
     * Used to implement a reference counting scheme.  This should atomically decrement the reference
     * count and return the result.  Once at zero, the resource should be scheduled for garbage
     * collection.
     *
     * Note, this is to be used by the {@link InternalServer} isntance and should not be invoked
     * directly.
     *
     * @return the release count.
     * @throws {@link IllegalStateException} if the retain count is zero
     */
    int release();

    /**
     * Gets the RequestHandler for the method.
     *
     * @param method the method name
     * @return the handler for the given method, never null
     *
     * @throws {@link NotFoundException} if the method cannot be found.
     * @throws {@link IllegalStateException} if the retain count is zero
     */
    InternalRequestPathHandler getHandler(final String method);

}
