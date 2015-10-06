package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 10/5/15.
 */
public interface EventObservationNameBuilder<ObservationT> {

    /**
     * Returns an instance of {@link EventObservationPathBuilder} for an event with the given name.
     *
     * @return the {@link EventObservationPathBuilder}
     */
    EventObservationTypeBuilder<ObservationT> named(final String name);

}
