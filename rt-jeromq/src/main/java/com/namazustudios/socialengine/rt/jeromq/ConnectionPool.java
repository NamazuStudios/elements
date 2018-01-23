package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Responsible for creating, configuring, and connecting instances of {@link Socket} and dispatching work against the
 * {@link Socket}.  Each {@link Socket} is wrapped in a {@link Connection} which can be used to perform network IO.
 *
 * The {@link Connection} manages the lifecycle of the {@link Socket} and provides startup and shutdown of its managed
 * connections.
 *
 */
public interface ConnectionPool {

    /**
     * Starts the {@link ConnectionPool}, blocking as necessary to startup and connect.  This accepts a {@link Function}
     * which will supply the {@link Socket} instances.  Note that each {@link Socket} supplied should be interchangeable
     * with any other as this pool will recycle {@link Socket} instances as needed.
     */
    default void start(Function<ZContext, Socket> socketSupplier) {
        start(socketSupplier, "");
    }

    /**
     * Starts the {@link ConnectionPool}, blocking as necessary to startup and connect.  This accepts a {@link Function}
     * which will supply the {@link Socket} instances.  Note that each {@link Socket} supplied should be interchangeable
     * with any other as this pool will recycle {@link Socket} instances as needed.
     */
    void start(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name);

    /**
     * Stops the {@link ConnectionPool}, blocking as necessary to stop all threads as well as close and destroy all
     * sockets in the pool.
     */
    void stop();

    /**
     * Performs some work in the {@link ConnectionPool}.  The pool selects an arbitrary {@link Socket} and then
     * provides an instance of {@link Connection} through which the various operations are performed.  The supplied
     * {@link Function<Connection, T>} shall be run on its own thread and must not block the calling scope.  The
     * returned {@link Future<T>} can be used to obtain the result of the {@link Function<Connection,T>}.  The caller
     * may opt to ignore the returnd {@link Future<T>} if the return value isn't interesting to the calling scope.
     *
     * Any exception raised from the supplied function will cause the underlying {@link Connection} to close to ensure
     * that both client and server remain in a consistent state.  If the supplied function needs to throw an exception
     * that will be relayed through the returned {@link Future#get()} method, then the {@link ExpectedException} must
     * be used to indicate so.
     *
     * @param tFunction the {@Function<Connection, T>} to handle the connection
     */
    <T> Future<T> process(Function<Connection, T> tFunction);

    /**
     * Invokes {@link #process(Function)} with a {@link Consumer} and returns a {@link Future<Void>}.
     *
     * @param consumer the {@link Consumer<Connection>}
     * @return the {@link Future<Void>}
     */
    default Future<Void> processV(final Consumer<Connection> consumer) {
        return process(connection -> {
            consumer.accept(connection);
            return null;
        });
    }

    /**
     * Gets the high water mark, that is the most connections that the {@link ConnectionPool} has held at a single time.
     * This persists beyond the scope of {@link #start(Function)} and {@link #stop()}.
     *
     * @return the high water mark for the lifetime of the {@link ConnectionPool}
     */
    int getHighWaterMark();

    /**
     * A special type of {@link ExecutionException} which is used to indicate an expected exception while running the
     * function passed to {@link #process(Function)} or {@link #processV(Consumer)}.  If thrown, this will signal the
     * {@link ConnectionPool} keep the associated {@link Connection} alive.
     */
    class ExpectedException extends RuntimeException {
        public ExpectedException(final Throwable cause) {
            super(cause);
        }
    }

}
