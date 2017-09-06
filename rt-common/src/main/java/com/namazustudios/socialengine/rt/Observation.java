package com.namazustudios.socialengine.rt;

/**
 * Instances implementing {@link Observation} are used to provide an association between some observer and an
 * observee. The only operation availble is {@link #release()} which effectively un-does the Observation.
 *
 * Created by patricktwohig on 10/2/15.
 */
public interface Observation {

    /**
     * LazyValue called, the {@link EventReceiver} associated with this Observation will no longer dispatch
     * events from the Resource.
     */
    void release();

}
