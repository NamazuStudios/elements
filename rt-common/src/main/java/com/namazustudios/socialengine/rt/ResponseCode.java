package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.manifest.http.HttpVerb;

import static java.lang.String.format;
import static java.util.Arrays.stream;

/**
 * Enumeration of the various server reserved response codes.  Each code is is essentially the ordinal of the enum, as
 * determined by {@link #ordinal()}, or'd with the {@link #SYSTEM_RESERVED_MASK}.  Client code shall not use error codes
 * that are masked by the {@link #SYSTEM_RESERVED_MASK} or this may interfere with remapping of errors.
 *
 * The only exception to the {@link #SYSTEM_RESERVED_MASK} is the {@link #OK} status, with a value of 0 to universally
 * indicate that there is no error.
 *
 * These codes may be mapped to other status codes (such as HTTP status codes), but are used to provide more
 * fine-grained information as to what exactly has failed internally.  Client code is free to use their own custom
 * response code format, so long as it does not conflict with any of the {@link #SYSTEM_RESERVED_MASK}.
 */
public enum ResponseCode {

    /**
     * The response was okay.  The only {@link ResponseCode} with a code value of 0.
     */
    OK {
        @Override
        public int getCode() { return 0; }
    },

    /**
     * The request was bad, malformed but could be re-attempted.
     */
    BAD_REQUEST_RETRY,

    /**
     * The request failed and should not be retried.
     */
    BAD_REQUEST_FATAL,

    /**
     * The content supplied in the request has invalid content or payload.
     */
    BAD_REQUEST_INVALID_CONTENT,

    /**
     * Indicates that we were trying to insert or create a duplicate resource.
     */
    DUPLICATE_RESOURCE,

    /**
     * Indicates that we were trying to register a task twice for the same task ID
     */
    DUPLICATE_TASK,

    /**
     * Indicates that the supplied task was not found.
     */
    TASK_NOT_FOUND,

    /**
     * Indicates that the supplied task was killed.
     */
    TASK_KILLED,

    /**
     * A particular path or resource ID could not be found.
     */
    RESOURCE_NOT_FOUND,

    /**
     * A resource was destroyed while waiting for a response.
     */
    RESOURCE_DESTROYED,

    /**
     * A resource has been destroyed and the client code is now operating with a dead handle to a Resource.
     */
    RESOURCE_DEAD,

    /**
     * Indicates that the requested asset is not found.
     */
    ASSET_NOT_FOUND,

    /**
     * Indicates that the manifest was not found.
     */
    MANIFEST_NOT_FOUND,

    /**
     * A module was not found by the resource loader
     */
    MODULE_NOT_FOUND,

    /**
     * A particular method could not be found at the requested path.
     */
    METHOD_NOT_FOUND,

    /**
     * A particular service could not be found at the requested path.
     */
    SERVICE_NOT_FOUND,

    /**
     * A particular service could not be found at the requested path.
     */
    MODEL_NOT_FOUND,

    /**
     * A particular operation, (eg {@link HttpOperation}) could not be found.
     */
    OPERATION_NOT_FOUND,

    /**
     * The system could not contact the app node with the given address.
     */
    NODE_NOT_FOUND,

    /**
     * No {@link HttpOperation} for the particular {@link HttpVerb} could be matched.
     */
    VERB_NOT_SUPPORTED,

    /**
     * No {@link HttpOperation} for the supplied Accept headers is available to service the request.
     */
    UNACCEPTABLE_CONTENT,

    /**
     * No {@link HttpOperation} for the supplied Content-Type header is available to service the request.
     */
    UNSUPPORTED_MEDIA_TYPE,

    /**
     * Used to relay a not found condition, other than method or path.
     */
    NOT_FOUND,

    /**
     * The request was canceled by the user.
     */
    USER_CANCELED_FATAL,

    /**
     * Auth failed, the request should be re-attempted with
     * new authentication credentails
     */
    FAILED_AUTH_RETRY,

    /**
     * Auth failed, the request should be abandoned.
     */
    FAILED_AUTH_FATAL,

    /**
     * indicates that the server cannot handle the current load
     */
    TOO_BUSY_FATAL,

    /**
     * The request timed out.  This may not actually sent by the server but may be supplied by the client to indicate
     * that the request timed out.
     */
    TIMEOUT_RETRY,

    /**
     * Indicates that the server encountered an unknown error.
     */
    INTERNAL_ERROR_FATAL,

    /**
     * Indicates the server encountered a bad manifest and the
     */
    INTERNAL_ERROR_BAD_MANIFEST_FATAL,

    /**
     * Indicates that there was a problem routing calls to a remote node.
     */
    ROUTING_EXCEPTION,

    /**
     * Indicates that a type has an invalid {@link NodeId}
     */
    INVALID_NODE_ID,

    /**
     * Indicates that a handler reached a timeout condition.
     */
    HANDLER_TIMEOUT,

    /**
     * Async operation was canceled.
     */
    ASYNC_OPERATION_CANCELED,

    /**
     * Indicates a custom response code.  A custom code is any code that is not masked by the
     * {@link #SYSTEM_RESERVED_MASK}.
     */
    CUSTOM,

    /**
     * Indicates an unknown code.  A code is {@link #UNKNOWN} when it is masked by the {@link #SYSTEM_RESERVED_MASK} but
     * cannot be identified using {@link #getCode()}.
     */
    UNKNOWN;

    /**
     * Masks {@link ResponseCode} values that are considered reserved by the system.
     */
    public static final int SYSTEM_RESERVED_MASK = 0x7FFF0000;

    /**
     * Gets the actual code as returned by {@link ResponseHeader#getCode()}.
     *
     * @return the response code
     */
    public int getCode() {
        return ordinal() << 16;
    }

    /**
     * Gets a descriptive string representing this {@link ResponseCode}.
     *
     * @return the {@link ResponseCode}
     */
    public String getDescription() {
        return format("%s (%x)", toString(), getCode());
    }

    /**
     * Gets the code for the value.
     *
     * @param code the {@link ResponseCode} for the provided code value.
     *
     * @return the {@link ResponseCode}
     */
    public static ResponseCode getCodeForValue(final int code) {
        return stream(values())
            .filter(c -> c.getCode() == code)
            .findFirst().orElse(isReserved(code) ? UNKNOWN : CUSTOM);
    }

    /**
     * Checks if the supplied
     * @param code
     * @return
     */
    public static boolean isReserved(final int code) {
        return code == 0 || ((SYSTEM_RESERVED_MASK & code) != 0);
    }

    /**
     * Returns a human readable description of the code.  This should always return a value.
     *
     * @param code the code
     * @return the description of the code
     */
    public static String getDescriptionFromCode(final int code) {
        return getCodeForValue(code).getDescription();
    }

}
