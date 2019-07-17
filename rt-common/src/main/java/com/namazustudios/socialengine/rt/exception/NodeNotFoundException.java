package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.id.NodeId;

import static java.lang.String.format;

public class NodeNotFoundException extends InternalException {

    public NodeNotFoundException() { }

    public NodeNotFoundException(String message) {
        super(message);
    }

    public NodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public NodeNotFoundException(final NodeId nodeId) {
        this(format("Node with ID not found %s", nodeId.asString()));
    }

    public NodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.NODE_NOT_FOUND;
    }
}
