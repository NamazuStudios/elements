package com.namazustudios.socialengine.rt;

/**
 * Returned when subscribing to an event using {@link Resource#subscribe(String, EventReceiver)}
 * and is used to later unsubscribe from the requested event.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface Subscription {

    /**
     * Once called, the {@link EventReceiver}, passed to {@link Resource#subscribe(String, EventReceiver)}
     * will no longer receive events from the {@link Resource}.
     */
    void release();

}
