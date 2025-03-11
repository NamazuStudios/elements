package dev.getelements.elements.sdk;

/**
 * Returned from the various subscribe calls. This interface represents an abract means to manage the subscription to
 * a particular event. The underlying implementation of the Subscription indicates the specific semantics. Beyond what
 * is documented here, make no assumptions as to the underlying implementation (eg thread safety) or its behavior.
 */
@FunctionalInterface
public interface Subscription {

    /**
     * Unsubscribes this subscription. Following this call, no future events will fire.
     */
    void unsubscribe();

    /**
     * Chains this {@link Subscription} to the other so that both will be unsubscribed at the same time. Useful for
     * if you wish to multiple related messages but want to easily unwind the subscription in one go.
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
     * Used to start a chain of {@link Subscription} instances. This returns a no-op subscription which serves as the
     * start of a chain.
     * 
     * @return a dummy {@link Subscription}
     */
    static Subscription begin() {
        return () -> {};
    }

}
