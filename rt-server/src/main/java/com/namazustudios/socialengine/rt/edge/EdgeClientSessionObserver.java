package com.namazustudios.socialengine.rt.edge;

/**
 * Used to dispatch events from the {@link EdgeClientSession}.  This
 */
public interface EdgeClientSessionObserver {

    /**
     * Called when the {@link EdgeClientSession} closes the session.  The return value is used
     * to determine if the {@link EdgeClientSessionObserver} should continue to receive
     * notifications.
     *
     * @return true to receive more notifications, false otherwise.
     */
    boolean observe();

}
