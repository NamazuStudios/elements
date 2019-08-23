package com.namazustudios.socialengine.rt;

/**
 * Returned from the various subscribe calls.  Can be used to cancel the subscription.
 */
@FunctionalInterface
public interface Subscription {

    /**
     * Unsubscribes from the
     */
    void unsubscribe();

}
