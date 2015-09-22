package com.namazustudios.socialengine.rt;

import java.net.SocketAddress;

/**
 * Manages the connections with the server and provides means to connect both instances of {@link Client}
 * as well as listen for the disconnect events.
 *
 * Created by patricktwohig on 9/12/15.
 */
public interface ClientContainer {

    /**
     * Connects to the given server.  If the connection succeeds, then this returns an
     * instance of {@link Client} which may be used to communicate with the server.
     *
     * Additionally any number of {@link DisconnectHandler} instances will be called when
     * the client disconnects.
     *
     * @param socketAddress
     */
    ConnectedInstance connect(SocketAddress socketAddress, DisconnectHandler ... disconnectHandlers);

    /**
     * Represents the connected client instance.
     */
    interface ConnectedInstance extends AutoCloseable {

        /**
         * Gets the reliable {@link Client} instance.
         *
         * @return the reliable instance
         */
        Client getRealiable();

        /**
         * Gets the best-effort {@link Client} instance.
         *
         * @return the best-effort instance
         */
        Client getBestEffort();

        /**
         * Disconnects from the server.
         */
        void disconnect();

        /**
         * Equivalent to calling {@link #disconnect()}
         */
        @Override
        void close();
    }

    /**
     * Used to indicate a disconnection event.
     */
    interface DisconnectHandler {

        /**
         * Called when a client has been disconnected.
         *
         * @param client the client that was disconnected
         * @param ex the exception which caused the disconnection, may be null
         */
        void didDisconnect(Client client, Exception ex);

    }

}
