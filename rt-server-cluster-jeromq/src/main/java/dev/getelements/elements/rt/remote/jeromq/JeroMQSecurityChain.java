package dev.getelements.elements.rt.remote.jeromq;

import org.zeromq.ZMQ;

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
    ZMQ.Socket server(ZMQ.Socket socket);

    /**
     * Configures the socket for use as a client socket.
     *
     * @param socket the socket
     *
     * @return the socket
     */
    ZMQ.Socket client(ZMQ.Socket socket);

    JeroMQSecurityChain DEFAULT = new JeroMQSecurityChain() {

        @Override
        public ZMQ.Socket client(final ZMQ.Socket socket) {
            socket.setPlainServer(false);
            socket.setCurveServer(false);
            socket.setCurvePublicKey(null);
            socket.setCurveSecretKey(null);
            socket.setCurveServerKey(null);
            return socket;
        }

        @Override
        public ZMQ.Socket server(ZMQ.Socket socket) {
            socket.setCurvePublicKey(null);
            socket.setCurveSecretKey(null);
            socket.setCurveServerKey(null);
            return socket;
        }

    };

}
