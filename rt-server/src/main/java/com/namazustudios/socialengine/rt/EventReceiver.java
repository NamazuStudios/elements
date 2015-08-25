package com.namazustudios.socialengine.rt;

/**
 * A type which receives events of a specific type.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface EventReceiver<PayloadT> {

    /**
     * Returns the event type for this receiver.
     *
     * @return the event type.
     */
    Class<PayloadT> getEventType();

    /**
     * Receives the event.  When the event is raised, the instance of
     * event is passed in to this method.
     *
     * @param path
     * @param name
     * @param event the event that was raised.
     */
    void receive(String path, String name, final PayloadT event);

}
