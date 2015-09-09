package com.namazustudios.socialengine.rt.edge;

/**
 * Represents a currently connected client.  The client also has associated
 * a set of session variables which can be accessed at any time.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface EdgeClient {

    /**
     * Gets the ID of the client.
     *
     * @return the id
     */
    String getId();

    /**
     * Sets the EdgeClient's session variable.
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
     * Disconnects the remote client.
     */
    void disconnect();

}
