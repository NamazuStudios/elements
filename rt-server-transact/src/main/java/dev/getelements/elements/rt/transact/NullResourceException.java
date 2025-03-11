package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

/**
 * Indicates that a {@link ResourceId} exists, but the
 */
public class NullResourceException extends InternalException {

    public NullResourceException() {}

    public NullResourceException(String message) {
        super(message);
    }

    public NullResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullResourceException(Throwable cause) {
        super(cause);
    }

    public NullResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
