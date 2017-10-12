package com.namazustudios.socialengine.rt.handler;

/**
 * Used to dispatch events from the {@link Session}.  This
 */
public interface HandlerClientSessionObserver {

    /**
     * Called when the {@link Session} closes the session.  The return value is used
     * to determine if the {@link HandlerClientSessionObserver} should continue to receive
     * notifications.
     *
     * @return true to receive more notifications, false otherwise.
     */
    void observe();

}
