package dev.getelements.elements.rt;

/**
 * Returned from the various subscribe calls.  Can be used to cancel the subscription.
 */
@FunctionalInterface
public interface Subscription {

    /**
     * Unsubscribes this subscription. Following this call, no future events will fire.
     */
    void unsubscribe();

    /**
     * Chains this {@link Subscription} to the other so that both will be unsubscribed at the same time. Useful for
     * if you wish to multiple related messages but want to easily unwind the subscription.
     *
     * @param other the other {@link Subscription} to which to chain this
     * @return a new {@link Subscription} that chains the this and the other.
     */
    default Subscription chain(final Subscription other) {
        return () -> {
            unsubscribe();
            other.unsubscribe();
        };
    }

    /**
     * Used to start a chain of {@link Subscription} instances.
     * 
     * @return a dummy {@link Subscription}
     */
    static Subscription begin() {
        return () -> {};
    }

}
