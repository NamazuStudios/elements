package com.namazustudios.socialengine.rt;

/**
 * Event objects are essentially server-to-client messages that do not come
 * in as part of a {@link Request}.  Rather, events, are send direct from
 * server to client.
 *
 * This has similar semantics to the {@link Response} class, however
 * this is simplified in that it is not generated in response to
 * a specific {@link Request}.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface Event {

    /**
     * Gets the sequence of the response.
     *
     * @return the sequence
     */
    int getSequence();

    /**
     * The path which sourced the event.
     *
     * @return the path of the event
     */
    String getPath();

}
