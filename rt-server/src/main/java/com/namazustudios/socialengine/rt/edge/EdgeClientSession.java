package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.internal.InternalResource;
import com.namazustudios.socialengine.rt.internal.InternalServer;

import java.util.List;

/**
 * Represents a currently connected client.  The client also has associated
 * a set of session variables which can be accessed at any time.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface EdgeClientSession {

    /**
     * Gets the ID of the client.  The ID is determined by the underlying framework and should
     * be used as an opaque identifier for the session.
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
     * Sets the EdgeClientSession's session variable if it is absent.  This will return
     * the value if it already exists.  Otehrwise will return a null and accept the
     * change into the session.
     *
     * @param key the key
     * @param value the value
     */
    Object setSessionVariableIfAbsent(Object key, Object value);

    /**
     * Gets the session variable with the given key.  If the key is not found,
     * then this returns null.
     *
     * @param key the key
     * @return the session variable
     */
    Object getSessionVariable(Object key);

    /**
     * Gets a session varible with the given key.  If the key is not found, then
     * this returns the specified default value.
     *
     * @param key the key
     * @param defaultValue they default value
     * @return the session variable
     */
    Object getSessionVariable(Object key, Object defaultValue);

    /**
     * Gets a session variable of the given key, type, and default value.
     * @param key
     * @param type
     *
     * @param <T>
     */
    <T> T getSessionVariableTyped(Object key, Class<T> type);

    /**
     * Gets a session variable of the given key, type, and default value.
     * @param key the session key
     * @param type the type
     * @param defaultValue the default value, if no session variable is found
     * @param <T>
     */
    <T> T getSessionVariableTyped(Object key, Class<T> type, T defaultValue);

    /**
     * Removes the session variable with the given key.
     *
     * @param key the session value key
     */
    void removeSessionVariable(Object key);

    /**
     * Adds a {@link EdgeClientSessionObserver} which will be called when the session is disconnected.
     */
    Observation observeDisconnect(EdgeClientSessionObserver edgeClientSessionObserver);

    /**
     * Adds a {@link EdgeClientSessionObserver} which will be called when the session goes idle.
     */
    Observation observeIdle(EdgeClientSessionObserver edgeClientSessionObserver);

    /**
     * Observes the the event with the given name, for the {@link EdgeServer} instance.  The {@link Observation}
     * is registered in this {@link EdgeClientSession} with a disconnection listener.  Upon termination
     * of this session, the container will automatically clean-up the session.
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    PathBuilder<EventObservationNameBuilder<Observation>> observeEdgeEvent();

    /**
     * Observes to the the event with the given name, for the {@link InternalServer} instance.  The
     * {@link Observation} is registered in this {@link EdgeClientSession} with a disconnection
     * listener.  Upon termination of this session, the container will automatically clean-up the session.
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    PathBuilder<EventObservationNameBuilder<Observation>> observeInternalEvent();

    /**
     * Disconnects the remote client.  This may not happen immediately.  This may allow the current session to
     * finish up work before actually closing the underlying transport.  However, once called it should
     * be assumed that the session is no longer in use and should not rely on any methods.
     *
     */
    void disconnect();

    /**
     * Created by patricktwohig on 10/5/15.
     */
    interface EventObservationNameBuilder<ObservationT> {

        /**
         * Returns an instance of {@link PathBuilder} for an event with the given name.
         *
         * @return the {@link PathBuilder}
         */
        EventObservationTypeBuilder<ObservationT> named(final String name);

    }

    /**
     * Used to specify the type of the event.
     */
    interface EventObservationTypeBuilder<ObservationT> {

        /**
         * Sets the type of the subscription to {@link Object}.
         *
         * @return an instance of the {@link Observation}
         */
        ObservationT ofAnyType();

        /**
         * Sets the type of the subscription to the given type.
         *
         * @return an instance of {@link PathBuilder}
         * @param type the name of the type.  Resolved using {@link Class#forName(String)}
         *
         * @return an instance of the {@link Observation}
         */
        ObservationT ofType(String type);

        /**
         * Sets the type of the subscription to the given type.
         *
         * @return an instance of {@link PathBuilder}
         * @param type the {@link Class} type for the event.
         * @param <T> the type of the event
         *
         * @return an instance of the {@link Observation}
         */
        <T> ObservationT ofType(Class<T> type);

    }

}
