package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.Observation;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.internal.InternalServer;

/**
 * Represents a currently connected client.  The client also has associated
 * a set of session variables which can be accessed at any time.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface EdgeClientSession {

    /**
     * Gets the ID of the client.
     *
     * @return the id
     */
    String getId();

    /**
     * Sets the EdgeClientSession's session variable.
     *
     * @param key the key
     * @param value the value
     */
    void setSessionVariable(Object key, Object value);

    /**
     * Gets a session variable of the given key, type, and default value.
     * @param key
     * @param type
     *
     * @param <T>
     */
    <T> T getSessionVariable(Object key, Class<T> type);

    /**
     * Gets a session variable of the given key, type, and default value.
     * @param key the session key
     * @param type the type
     * @param defaultValue the default value, if no session variable is found
     * @param <T>
     */
    <T> T getSessionVariable(Object key, Class<T> type, T defaultValue);

    /**
     * Removes the session variable with the given key.
     *
     * @param key the session value key
     */
    void removeSessionVariable(Object key);

    /**
     * Adds a {@link EdgeClientSessionListener} which will
     * be called when the session goes idle.
     */
    void addIdleListener(EdgeClientSessionListener edgeClientSessionListener);

    /**
     * Adds a {@link EdgeClientSessionListener} which will
     * be called when the session goes idle.
     */
    void addDisconnectListener(EdgeClientSessionListener edgeClientSessionListener);

    /**
     * Observes the the event with the given name, for the {@link EdgeServer} instance.  The {@link Subscription}
     * is registered in this {@link EdgeClientSession} with a disconnection listener.  Upon termination
     * of this session, the container will automatically clean-up the session.
     *
     * @param name the name of the event
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationTypeBuilder<Observation> observeEdgeEvent(String name);

    /**
     * Observes to the the event with the given name, for the {@link InternalServer} instance.  The
     * {@link Subscription} is registered in this {@link EdgeClientSession} with a disconnection
     * listener.  Upon termination of this session, the container will automatically clean-up the session.
     *
     * @param name the name of the event
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationTypeBuilder<Observation> observeInternalEvent(String name);

    /**
     * Subscribes to the the event with the given name, for the {@link EdgeServer} instance.  The {@link Subscription}
     * is registered in this {@link EdgeClientSession} with a disconnection listener.  Upon termination
     * of this session, the container will automatically clean-up the session.
     *
     * @param name the name of the event
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationTypeBuilder<Subscription> subscribeToEdgeEvent(String name);

    /**
     * Subscribes to the the event with the given name, for the {@link InternalServer} instance.  The
     * {@link Subscription} is registered in this {@link EdgeClientSession} with a disconnection
     * listener.  Upon termination of this session, the container will automatically clean-up the session.
     *
     * @param name the name of the event
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationTypeBuilder<Subscription> subscribeToInternalEvent(String name);

    /**
     * Disconnects the remote client.
     */
    void disconnect();

    /**
     * Used to specify the type of the event.
     */
    interface EventObservationTypeBuilder<ObservationT extends Observation> {

        /**
         * Sets the type of the subscription to {@link Object}.
         *
         * @return an instance of {@link EventObservationBuilder}
         */
        EventObservationBuilder<ObservationT> ofAnyType();

        /**
         * Sets the type of the subscription to the given type.
         *
         * @return an instance of {@link EventObservationBuilder}
         * @param type the name of the type.  Resolved using {@link Class#forName(String)}
         *
         * @return an instance of {@link EventObservationBuilder}
         */
        EventObservationBuilder<ObservationT> ofType(String type);

        /**
         * Sets the type of the subscription to the given type.
         *
         * @return an instance of {@link EventObservationBuilder}
         * @param type the {@link Class} type for the event.
         * @param <T> the type of the event
         *
         * @return an instance of {@link EventObservationBuilder}
         */
        <T> EventObservationBuilder<ObservationT> ofType(Class<T> type);

    }

    /**
     * Used to specify the location of the event as a path.
     */
    interface EventObservationBuilder<ObservationT extends Observation> {

        /**
         * Completes the subscription process with the path.  Events from the {@link Resource} will
         * begin receiving the events.
         *
         * @param path the path (as a string)
         * @return
         */
        ObservationT atPath(String path);

        /**
         * Completes the subscription process with the path.  Events from the {@link Resource} will
         * begin receiving the events.
         *
         * @param path the path as an object
         * @return the {@link Subscription} instance
         */
        ObservationT atPath(Path path);

    }

}
