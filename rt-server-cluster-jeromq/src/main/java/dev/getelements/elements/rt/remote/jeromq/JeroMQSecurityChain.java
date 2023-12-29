package dev.getelements.elements.rt.remote.jeromq;

import org.zeromq.ZMQ;

import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * Implements a security chain for JeroMQ Sockets.
 */
public interface JeroMQSecurityChain {

    /**
     * Configures the socket for use as a server socket.
     *
     * @param socket the socket
     *
     * @return the socket
     */
    default ZMQ.Socket server(ZMQ.Socket socket) {
        return server(() -> socket);
    }

    /**
     * Configures the socket for use as a server socket.
     *
     * @param socketSupplier the {@link Supplier} for the {@link ZMQ.Socket}
     *
     * @return the socket
     */
    ZMQ.Socket server(Supplier<ZMQ.Socket> socketSupplier);

    /**
     * Configures the socket for use as a client socket.
     *
     * @param socket the socket
     *
     * @return the socket
     */
    default ZMQ.Socket client(ZMQ.Socket socket) {
        return client(() -> socket);
    }

    /**
     * Configures the socket for use as a client socket.
     *
     * @param socketSupplier the {@link Supplier} for the {@link ZMQ.Socket}
     *
     * @return the socket
     */
    ZMQ.Socket client(Supplier<ZMQ.Socket> socketSupplier);

    /**
     * The default {@link JeroMQSecurityChain} which simply returns the {@link org.zeromq.ZMQ.Socket} as provided with
     * no alterations.
     */
    JeroMQSecurityChain DEFAULT = new JeroMQSecurityChain() {

        @Override
        public ZMQ.Socket client(final Supplier<ZMQ.Socket> socketSupplier) {
            return socketSupplier.get();
        }

        @Override
        public ZMQ.Socket server(final Supplier<ZMQ.Socket> socketSupplier) {
            return socketSupplier.get();
        }

        @Override
        public String toString() {
            return format("%s.DEFAULT", JeroMQSecurityChain.class.getName());
        }

    };

}
