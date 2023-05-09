package dev.getelements.elements.testsources;

public interface TestConstant {

    /**
     * Used to trigger a Long-Polling type of requests.  This requests that the server wait on a response until certain
     * conditions trigger a response.  The server will make its best effort attempt to wait until either the response is
     * ready or the timeout hits.  However, the serer may elect to terminate the request sooner than specified.
     *
     * A value of 0 indicates that the server should determine timeout, but hold the requests for as long as reasonably
     * possible.
     */
    String REQUEST_LONG_POLL_TIMEOUT = "SocialEngine-LongPoll-Timeout";

}
