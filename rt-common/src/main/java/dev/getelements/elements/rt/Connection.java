package dev.getelements.elements.rt;

/**
 * Represents a connection to the remote node.
 */
public interface Connection<ContextT, SocketT> extends AutoCloseable {

    /**
     * Returns the context used by this {@link Connection}.
     *
     * @return the context instance
     */
    ContextT context();

    /**
     * Obtains the context instance used to communicate with the remote node.  This must always return the
     * same instance of context per {@link Connection} instance
     *
     * @return the socket instance
     */
    SocketT socket();

    /**
     * Closes the {@link Connection} an destroys the associated underlying socket.
     */
    @Override
    void close();

}
