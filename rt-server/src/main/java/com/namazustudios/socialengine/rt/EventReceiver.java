package com.namazustudios.socialengine.rt;

/**
 * A type which receives events of a specific type.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface EventReceiver<EventT> {

    /**
     * Returns the event type for this receiver.
     *
     * @return the event type.
     */
    Class<EventT> getEventType();

    /**
     * Receives the event.  When the event is raised, the instance of
     * event is passed in to this method.
     *
     * @param event the event that was raised.
     */
    void receive(final EventT event);

}
