package com.namazustudios.socialengine.rt;

/**
 * Instances implenting {@link Observation} are used to provide an association between some source of {@link Event}
 * instances and the {@link EventReceiver}.
 *
 * The only operation availble is {@link #release()} which effectively un-does the Observation.  A more specific
 * type of Observation, {@link Subscription}, can give more context.
 *
 * Created by patricktwohig on 10/2/15.
 */
public interface Observation {

    /**
     * Once called, the {@link EventReceiver} associated with this Observation will no longer dispatch
     * events from the Resource.
     */
    void release();
}
