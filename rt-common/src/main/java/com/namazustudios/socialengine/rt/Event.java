package com.namazustudios.socialengine.rt;

/**
 * Event objects are essentially server-to-client messages that do not come
 * in as part of a {@link Request}.  Rather, events, are send direct from
 * server to client.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface Event {

    /**
     *
     *
     * @return the path of the event
     */
    String getPath();

}
