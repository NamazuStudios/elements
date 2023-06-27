package dev.getelements.elements.rt;

/**
 * EventHeader objects are essentially server-to-client messages that do not come
 * in as part of a {@link RequestHeader}.  Rather, events, are send direct from
 * server to client.
 *
 * This has similar semantics to the {@link ResponseHeader} class, however
 * this is simplified in that it is not generated in response to
 * a specific {@link RequestHeader}.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface EventHeader {

    /**
     * Gets the path that sourced this event.
     *
     * @return the path that sourced this event.
     */
    String getPath();

    /**
     * Gets the name of the event.
     *
     * @return the name of the event
     */
    String getName();

}
