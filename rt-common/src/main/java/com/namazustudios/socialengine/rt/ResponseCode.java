package com.namazustudios.socialengine.rt;

/**
 * Enumeration of the various response codes.  Each code is essentially the
 * ordinal of the enum, which should be fetched using {@link #getCode()}.
 *
 */
public enum ResponseCode {

    /**
     * The response was okay.
     */
    OK,

    /**
     * The request was bad, malformed but could be re-attempted.
     */
    BAD_REQUEST_RETRY,

    /**
     * The request failed and should not be retried.
     */
    BAD_REQUEST_FATAL,

    /**
     * A particular path could not be found.
     */
    RESOURCE_NOT_FOUND,

    /**
     * Indicates that we were trying to insert or create a duplicate resource.
     */
    DUPLICATE_RESOURCE,

    /**
     * A particular metohd could not be found at the requested path.
     */
    METHOD_NOT_FOUND,

    /**
     * Used to relay a not found condition, other than method or path.
     */
    OTHER_NOT_FOUND,

    /**
     * Auth failed, the request should be re-attempted with
     * new authentication credentails
     */
    FAILED_AUTH_RETRY,

    /**
     * The request timed out.  This may not actually sent by the server
     * but may be supplied by the client to indicate that the request
     * timed out.
     */
    TIMEOUT_RETRY,

    /**
     * The request was canceled by the user.
     */
    USER_CANCELED_FATAL,

    /**
     * Auth failed, the request should be abandoned.
     */
    FAILED_AUTH_FATAL,

    /**
     * indicates that the server cannot handle the current load
     */
    TOO_BUSY_FATAL,

    /**
     * Indicates that the requested asset is not found.
     */
    ASSET_NOT_FOUND,

    /**
     * Indicates that the manifest was not found.
     */
    MANIFEST_NOT_FOUND,

    /**
     * Indicates that the server encountered an unknown error.
     */
    INTERNAL_ERROR_FATAL,

    /**
     * Indicates the server encountered a bad manifest and the
     */
    INTERNAL_ERROR_BAD_MANIFEST_FATAL;

    /**
     * Gets the actual code as returned by {@link ResponseHeader#getCode()}.
     *
     * @return the response code
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns a human readable description of the code.
     *
     * @param code the code.
     * @return the description of the code
     */
    public static String getDescriptionFromCode(final int code) {
        final ResponseCode[] values = values();
        return (code < values.length) ? (values[code].toString()) : String.format("CUSTOM(%d)", code);
    }

}
