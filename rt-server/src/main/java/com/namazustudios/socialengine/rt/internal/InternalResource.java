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
     * @return the retain count
     * @throws {@link IllegalStateException} if the retain count is zero or less
     */
    int retain();

    /**
     * Used to implement a reference counting scheme.  This should atomically decrement the reference
     * count and return the result.  Once at zero, the resource will be scheduled for garbage
     * collection and removed from the server.
     *
     * @return the release count.
     * @throws {@link ZeroReferenceCountException} if the retain count is zero or less
     */
    int release();

    /**
     * Gets the RequestHandler for the method.
     *
     * @param method the method name
     * @return the handler for the given method, never null
     *
     * @throws {@link NotFoundException} if the method cannot be found.
     * @throws {@link ZeroReferenceCountException} if the retain count is zero
     */
    InternalRequestPathHandler getHandler(final String method);

    /**
     * Used to indicate that the reference count has hit zero.
     */
    class ZeroReferenceCountException extends IllegalStateException {

        public ZeroReferenceCountException() {}

        public ZeroReferenceCountException(String s) {
            super(s);
        }

        public ZeroReferenceCountException(String message, Throwable cause) {
            super(message, cause);
        }

        public ZeroReferenceCountException(Throwable cause) {
            super(cause);
        }

    }

}
