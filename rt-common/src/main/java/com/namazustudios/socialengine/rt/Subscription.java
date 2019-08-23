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

    /**
     * Chains this {@link Subscription to the other so that both will be unsubscribed.}
     *
     * @param other the other {@link Subscription} to which to chain this
     * @return a new {@link Subscription} that chains the this and the other.
     */
    default Subscription chain(final Subscription other) {
        return () -> {
            unsubscribe();
            other.unsubscribe();};
    }

    /**
     * Used to start a chain of {@link Subscription} instances.
     * @return a dummy {@link Subscription}
     */
    static Subscription begin() {
        return () -> {};
    }

}
