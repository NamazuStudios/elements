package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.Observation;
import com.namazustudios.socialengine.rt.exception.InvalidConversionException;

/**
 * Represents a currently connected client.  The client also has associated
 * a set of session variables which can be accessed at any time.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface Session {

    /**
     * Gets the ID of the client.  The ID is determined by the underlying framework and should be used as an opaque
     * identifier for the session.
     *
     * @return the id
     */
    String getId();

    /**
     * Sets the Session's session variable.
     *
     * @param key the key
     * @param value the value
     */
    void setSessionVariable(Object key, Object value);

    /**
     * Sets the Session's session variable if it is absent.  This will return the value if it already exists.
     * Otherwise will return a null and accept the change into the session.
     *
     * @param key the key
     * @param value the value
     * @return the current value, or null if the variable was absent and successfully set
     */
    Object setSessionVariableIfAbsent(Object key, Object value);

    /**
     * Gets the session variable with the given key.  If the key is not found, then this returns null.
     *
     * @param key the key
     * @return the session variable
     */
    default Object getSessionVariable(Object key) {
        return getSessionVariable(key, null);
    }

    /**
     * Gets a session varible with the given key.  If the key is not found, then this returns the specified default
     * value.
     *
     * @param key the key
     * @param defaultValue they default value
     * @return the session variable
     */
    Object getSessionVariable(Object key, Object defaultValue);

    /**
     * Gets a session variable of the given key, type, and default value.
     *
     * @param key
     * @param type
     *
     * @param <T>
     */
    default <T> T getSessionVariableTyped(final Object key, final Class<T> type) {
        return getSessionVariableTyped(key, type, null);
    }

    /**
     * Gets a session variable of the given key, type, and default value.
     * @param key the session key
     * @param type the type
     * @param defaultValue the default value, if no session variable is found
     * @param <T>
     */
    default <T> T getSessionVariableTyped(final Object key, final Class<T> type, final T defaultValue) {
        try {
            final Object value = getSessionVariable(key);
            return type.cast(value);
        } catch (ClassCastException ex) {
            throw new InvalidConversionException(ex);
        }
    }

    /**
     * Removes the session variable with the given key.
     *
     * @param key the session value key
     */
    void removeSessionVariable(Object key);

    /**
     * Adds a {@link HandlerClientSessionObserver} which will be called when the session goes idle.
     */
    Observation observeIdle(HandlerClientSessionObserver handlerClientSessionObserver);

    /**
     * Adds a {@link HandlerClientSessionObserver} which will be called when the session is disconnected.
     */
    Observation observeDisconnect(HandlerClientSessionObserver handlerClientSessionObserver);

    /**
     * Disconnects the remote client.  This may not happen immediately.  This may allow the current session to
     * finish up work before actually closing the underlying transport.  However, once called it should
     * be assumed that the session is no longer in use and should not rely on any methods.
     *
     */
    void disconnect();

}
