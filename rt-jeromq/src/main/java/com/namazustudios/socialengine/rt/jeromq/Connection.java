package com.namazustudios.socialengine.rt.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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

    default void sendMessage(ZMsg msg) {
        msg.send(socket());
    }

    /**
     * Closes the {@link Connection} an destroys the associated underlying {@link Socket}.
     */
    @Override
    void close();

    /**
     *
     * {@see {@link #from(ZContext, Function, Logger) }
     *
     * @param context the {@link ZContext}
     * @param socketSupplier the {@link Function<ZContext, Socket>}
     * @return the {@link Connection}
     * @throws IllegalArgumentException if the supplied {@link Socket} is not part of the {@link ZContext}
     */
    static Connection from(final ZContext context, final Function<ZContext, Socket> socketSupplier) {
        final Logger logger = LoggerFactory.getLogger(Connection.class);
        return from(context, socketSupplier, logger);
    }

    /**
     * Creates a {@link Connection} from the supplied {@link ZContext} and {@link Function<ZContext, Socket>}.  The returned
     * {@link Connection} will automatically close and destroy the link {@link Socket} returned by the
     * supplier.
     *
     * The {@link Function<ZContext, Socket>} is only called once and the return value cached in the provided
     * {@link Connection}.
     *
     * @param context the {@link DynamicConnectionPool.Context}
     * @param socketSupplier the {@link Function<ZContext, Socket>}
     * @param logger the {@link Logger} to use
     * @return the {@link Connection}
     * @throws IllegalArgumentException if the supplied {@link Socket} is not part of the {@link ZContext}
     */
    static Connection from(final ZContext context, final Function<ZContext, Socket> socketSupplier, final Logger logger) {

        final Socket socket = socketSupplier.apply(context);
        final AtomicBoolean open = new AtomicBoolean(true);

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

                if (!open.compareAndSet(true, false)) throw new IllegalStateException("Connection closed.");

                try {
                    socket.close();
                } catch (final Exception ex) {
                    logger.error("{} Caught exception closing Socket.", toString(), ex);
                }

                try {
                    context.destroySocket(socket);
                } catch (final Exception ex) {
                    logger.error("{} Caught exception destroying Socket.", toString(), ex);
                }

                logger.debug("Successfully closed socket {} ", socket);

            }
        };

    }
}
