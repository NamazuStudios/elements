package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a connection to the remote node.
 */
public interface Connection extends AutoCloseable {

    /**
     * Returns the {@link ZContext} used by this {@link Connection}.
     *
     * @return the {@link ZContext} instance
     */
    ZContext context();

    /**
     * Obtains the {@link Socket} instance used to communicate with the remote node.  This must always return the
     * same instance of {@link Socket} per {@link Connection} instance
     *
     * @return the {@link Socket} instance
     */
    Socket socket();

    /**
     * Closes the {@link Connection} an destroys the associated underlying {@link Socket}.
     */
    @Override
    void close();

    /**
     * Creates a {@link Connection} from the supplied {@link ZContext} and {@link Function<ZContext, Socket>}.  The returned
     * {@link Connection} will automatically close and destroy the link {@link Socket} returned by the
     * supplier.
     *
     * The {@link Function<ZContext, Socket>} is only called once and the return value cached in the provided
     * {@link Connection}.
     *
     * @param context the {@link com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.Context}
     * @param socketSupplier the {@link Function<ZContext, Socket>}
     * @return the {@link Connection}
     * @throws IllegalArgumentException if the supplied {@link Socket} is not part of the {@link ZContext}
     */
    static Connection from(final ZContext context, final Function<ZContext, Socket> socketSupplier) {

        final Socket socket = socketSupplier.apply(context);

        if (!context.getSockets().contains(socket)) {
            throw new IllegalArgumentException("Returned socket must match the connection.");
        }

        return new Connection() {
            @Override
            public ZContext context() {
                return context;
            }

            @Override
            public Socket socket() {
                return socket;
            }

            @Override
            public void close() {
                context.destroySocket(socket);
            }
        };

    }
}
