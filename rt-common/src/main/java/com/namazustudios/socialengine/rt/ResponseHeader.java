package com.namazustudios.socialengine.rt;

/**
 *
 * The basic response type for the RT Server.  This is compact set of
 * metadata for a single request.  This has some terminology similar to
 * HTTP.
 *
 * Note that this really only includes the information preceeding a request
 * and the actual payload of the request follows this object in the stream
 * or packet.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ResponseHeader {

    /**
     * Gets the response code.
     *
     * @return
     */
    int getCode();

    /**
     * Gets the sequence of the response.
     *
     * @return the sequence
     */
    int getSequence();

    /**
     * Gets the response code.
     */
    enum Code {

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
        PATH_NOT_FOUND,

        /**
         * A particular metohd could not be found at the requested path.
         */
        METHOD_NOT_FOUND,

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
         * Indicates that the server encountered an unknown or internal error.
         */
        INTERNAL_ERROR_FATAL;

        /**
         * Gets the actual code as returned by {@link ResponseHeader#getCode()}.
         *
         * @return the response code
         */
        public int getCode() {
            return ordinal();
        }

    }

}
