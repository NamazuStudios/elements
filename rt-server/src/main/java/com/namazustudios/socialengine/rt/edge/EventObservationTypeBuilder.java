package com.namazustudios.socialengine.rt.edge;

/**
 * Used to specify the type of the event.
 */
public interface EventObservationTypeBuilder<ObservationT> {

    /**
     * Sets the type of the subscription to {@link Object}.
     *
     * @return an instance of {@link EventObservationPathBuilder}
     */
    EventObservationPathBuilder<ObservationT> ofAnyType();

    /**
     * Sets the type of the subscription to the given type.
     *
     * @return an instance of {@link EventObservationPathBuilder}
     * @param type the name of the type.  Resolved using {@link Class#forName(String)}
     *
     * @return an instance of {@link EventObservationPathBuilder}
     */
    EventObservationPathBuilder<ObservationT> ofType(String type);

    /**
     * Sets the type of the subscription to the given type.
     *
     * @return an instance of {@link EventObservationPathBuilder}
     * @param type the {@link Class} type for the event.
     * @param <T> the type of the event
     *
     * @return an instance of {@link EventObservationPathBuilder}
     */
    <T> EventObservationPathBuilder<ObservationT> ofType(Class<T> type);

}
