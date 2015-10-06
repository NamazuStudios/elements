package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;
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
     * Sets the EdgeClientSession's session variable if it is absent.  This will return
     * the value if it already exists.  Otehrwise will return a null and accept the
     * change into the session.
     *
     * @param key the key
     * @param value the value
     */
    Object setSessionVariableIfAbsent(Object key, Object value);

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
     * Adds a {@link EdgeClientSessionObserver} which will be called when the session is disconnected.
     */
    Observation observeDisconnect(EdgeClientSessionObserver edgeClientSessionObserver);

    /**
     * Adds a {@link EdgeClientSessionObserver} which will be called when the session goes idle.
     */
    Observation observeIdle(EdgeClientSessionObserver edgeClientSessionObserver);

    /**
     * Observes the the event with the given name, for the {@link EdgeServer} instance.  The {@link Subscription}
     * is registered in this {@link EdgeClientSession} with a disconnection listener.  Upon termination
     * of this session, the container will automatically clean-up the session.
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationNameBuilder<Observation> observeEdgeEvent();

    /**
     * Observes to the the event with the given name, for the {@link InternalServer} instance.  The
     * {@link Subscription} is registered in this {@link EdgeClientSession} with a disconnection
     * listener.  Upon termination of this session, the container will automatically clean-up the session.
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationNameBuilder<Observation> observeInternalEvent();

    /**
     * Subscribes to the the event with the given name, for the {@link EdgeServer} instance.  The {@link Subscription}
     * is registered in this {@link EdgeClientSession} with a disconnection listener.  Upon termination
     * of this session, the container will automatically clean-up the session.
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationNameBuilder<List<Subscription>> subscribeToEdgeEvent();

    /**
     * Subscribes to the the event with the given name, for the {@link InternalServer} instance.  The
     * {@link Subscription} is registered in this {@link EdgeClientSession} with a disconnection
     * listener.  Upon termination of this session, the container will automatically clean-up the session.
     *
     * @return a {@link EventObservationTypeBuilder} instance, used to build the rest of the subscription
     */
    EventObservationNameBuilder<List<Subscription>> subscribeToInternalEvent();

    /**
     * Disconnects the remote client.
     */
    void disconnect();

}
