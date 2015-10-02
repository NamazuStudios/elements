package com.namazustudios.socialengine.rt;

/**
 * A type which receives events of a specific type.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface EventReceiver<PayloadT> {

    /**
     * Returns the event type for this receiver.  Resource instances of this type
     * will only deliver the event if both the name and the object are compatible.
     *
     * If the return value from this method is not compatible with the event sourced, then
     * the Resource must skip over this receiver (and possibly log a warning).
     *
     * In order to dispatch all events, implementors of this interface must make the type
     * as generic as possible to ensure they will dispatch the event sourced.
     *
     * @return the event type.
     */
    Class<PayloadT> getEventType();

    /**
     * Receives the event.  When the event is raised, the instance of
     * event is passed in to this method.
     *  @param path
     * @param name
     * @param event the event that was raised.
     */
    void receive(Path path, String name, final PayloadT event);

}
