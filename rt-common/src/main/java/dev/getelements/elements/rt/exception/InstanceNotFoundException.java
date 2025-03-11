package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;
import dev.getelements.elements.sdk.cluster.id.InstanceId;

import static java.lang.String.format;

public class InstanceNotFoundException extends RoutingException {
    public InstanceNotFoundException() { }

    public InstanceNotFoundException(final String message) {
        super(message);
    }

    public InstanceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InstanceNotFoundException(final Throwable cause) {
        this(format("Instance with ID not found %s", "<unknown>"), cause);
    }

    public InstanceNotFoundException(final InstanceId instanceId) {
        this(format("Instance with ID not found %s", instanceId.asString()));
    }
    public InstanceNotFoundException(final InstanceId instanceId, Throwable cause) {
        this(format("Instance with ID not found %s", instanceId.asString()), cause);
    }

    public InstanceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.NODE_NOT_FOUND;
    }
}
