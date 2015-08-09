package com.namazustudios.socialengine.rt;

/**
 * Represents a one-way message produced by a Resource, and dispatched to various
 * clients both internal and external to the system.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface Event {

    /**
     * Gets the event header.
     *
     * @return the event heder
     */
    EventHeader getEventHeader();

    /**
     * Gets the payload for the event.
     *
     * @return the payload
     */
    Object getPayload();

}
