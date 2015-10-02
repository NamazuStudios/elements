package com.namazustudios.socialengine.rt;

/**
 * Returned when subscribing to an event using and is used to later un-subscribe from the requested event.
 *
 * This is a refinedment of the {@link Observation} instance isn that a {@link Subscription} effectively
 * follows the source of the event.  For example, if a Resource moves, the path at which the events are
 * served will change.  However, the a resubscription is not necessary as the subscription follows the
 * resource.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface Subscription extends Observation {

    /**
     * Gets the {@link Path} for this subscription.  Note, the {@link Path} may change if the
     * target resource moves or is deleted.  It is not recommended to map based on the path.
     *
     * @return the path
     */
    Path getPath();

}
