package com.namazustudios.socialengine.rt;

/**
 * Returned by a {@link Client} when making a {@link Request}.  This provides the option
 * for the client to cancel the request.
 *
 * Created by patricktwohig on 9/4/15.
 */
public interface PendingRequest {

    /**
     * Cancels the request.  Equivalent to calling {@link #cancel(boolean)} with silent set to
     * false.
     */
    void cancel();

    /**
     * Cancels the request.  Any {@link ResponseReceiver} will be given an {@link Response}
     * with code {@link ResponseCode#USER_CANCELED_FATAL} if the silent flag is set to false.
     *
     * @param silent set to false to send any {@link ResponseReceiver} instances a simulated canceled response
     */
    void cancel(boolean silent);

}
