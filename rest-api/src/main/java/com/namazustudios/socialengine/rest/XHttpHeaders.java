package com.namazustudios.socialengine.rest;

/**
 * A place to store the non-standard HTTP header (and related) constants..
 *
 * Created by patricktwohig on 6/26/17.
 */
public interface XHttpHeaders {

    /**
     * Used in conjunction with the standard Authorization header.  This is used to
     * trigger an attempt to authorize the user via Facebook OAuth tokens.
     */
    String AUTH_TYPE_FACEBOOK = "Facebook";

    /**
     * Used to trigger a Long-Polling type of requests.  This requests that the server
     * wait on a response until certain conditions trigger a response.  The server
     * will make its best effort attempt to wait until either the response is ready
     * or the timeout hits.  However, the serer may elect to terminate the request
     * sooner than specified.
     *
     * A value of 0 indicates that the server should determine timeout, but hold
     * the requests for as long as reasonably possible.
     */
    String X_REQUEST_LONG_POLL_TIMEOUT = "X-SocialEngine-LongPoll-Timeout";

}
