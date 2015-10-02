package com.namazustudios.socialengine.rt.edge;

/**
 * Used to dispatch events from the {@link EdgeClientSession}.  This
 */
public interface EdgeClientSessionListener {

    /**
     * Called when the {@link EdgeClientSession} closes the session.
     */
    void receive();

}
