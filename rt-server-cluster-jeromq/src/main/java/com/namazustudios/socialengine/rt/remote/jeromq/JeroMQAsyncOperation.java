package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.remote.AsyncOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQAsyncOperation.State.*;
import static java.lang.Runtime.getRuntime;

public class JeroMQAsyncOperation implements AsyncOperation {

    private static final int THREAD_COUNT = getRuntime().availableProcessors() + 1;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncOperation.class);

    private static final ScheduledExecutorService cancelTimer = Executors.newScheduledThreadPool(THREAD_COUNT);

    private final AsyncConnectionPool<ZContext, ZMQ.Socket> pool;

    private final AtomicReference<ConnectionState> state = new AtomicReference<>(new ConnectionState());

    public JeroMQAsyncOperation(AsyncConnectionPool<ZContext, ZMQ.Socket> pool) {
        this.pool = pool;
    }

    public boolean finish() {

    }

    @Override
    public void cancel() {
        state.updateAndGet(current -> {
            switch (current.state) {
                case FINISHED:
                case CANCELED:
                    // These states indicate that the connection has already finished and no actions
                    // are necessary. The connection state will simply do nothing when it is processed
                    return current;
                case CONNECTION_PENDING:
                    // We haven't even gotten to acquiring a connection. When a connection is acquired, it will
                    // be the responsibility of the caller to recycle the connection before it has been acaquired.
                    return current.update(CANCELED);
                case CONNECTION_ACQUIRED:
                    // The connection was accquired and we are waiting on a response. This means that the connection
                    // is now in an undefined state. Therefore the connection must be destroyed.
                    return current.update(CANCELLATION_PENDING);
                default:
                    // This is here for future proofing.
                    throw new IllegalStateException("Invalid state: " + current);
            }
        }).process();
    }

    @Override
    public void timeout(final long time, final TimeUnit timeUnit) {
        cancelTimer.schedule(this::cancel, time, timeUnit);
    }

    /**
     * Called when the connection is acquired. If the operation is still in a suitable state, this will return true
     * incicating that the operation may proceed. This is only valid if the current state is
     * {@link State#CONNECTION_PENDING}. For any other state, this will return false indicating that the connection
     * has already finished or is in the process of being canceled.
     *
     * @param connection the connection that was acquired
     * @return true if the operation may proceed.
     */
    public boolean acquire(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
        final var pending = new ConnectionState(CONNECTION_PENDING, null);
        return state.compareAndSet(pending, pending.update(CONNECTION_ACQUIRED, connection));
    }

    public enum State {

        /**
         * Indicates that the operation is acquiring a connection from the connection pool.
         */
        CONNECTION_PENDING,

        /**
         * Indicates that the operation has acquired a connection and we are waiting for the operation to finish.
         */
        CONNECTION_ACQUIRED,

        /**
         * Indicates that the operation is pending cancellation, but the cancellation has not taken place.
         */
        CANCELLATION_PENDING,

        /**
         * Indicates that the invocation has been canceled successfully. This is a terminal state. Once in this state
         * no other state changes may happen.
         */
        CANCELED,

        /**
         * Indicates that the invocation has finished and the connection has been returned to the connection pool.
         */
        FINISHED

    }

    private class ConnectionState {

        private final State state;

        private final AsyncConnection<ZContext, ZMQ.Socket> connection;

        private ConnectionState() {
            this.state = State.CONNECTION_PENDING;
            this.connection = null;
        }

        private ConnectionState(final State state, final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            this.state = state;
            this.connection = connection;
        }

        private ConnectionState update(final State state) {
            return new ConnectionState(state, connection);
        }

        private ConnectionState update(final State state, final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            return new ConnectionState(state, connection);
        }

        private void process() {
            switch (state) {
                case FINISHED:
                    break;
                case CANCELLATION_PENDING:
                    destroy();
                    break;
                default:
                    logger.debug("No action needed for state {}", state);
            }
        }

        private void destroy() {
            connection.signal(zContextSocketAsyncConnection -> {

                final var expected = new ConnectionState(CANCELLATION_PENDING, null);

                if (!JeroMQAsyncOperation.this.state.compareAndSet(expected, expected.update(CANCELED))) {
                    logger.error("Expected state {} but got {} instead.", CANCELLATION_PENDING, CANCELED);
                }

                zContextSocketAsyncConnection.close();

            });
        }

        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            if (!super.equals(object)) return false;
            ConnectionState that = (ConnectionState) object;
            return state == that.state;
        }

        public int hashCode() {
            return java.util.Objects.hash(super.hashCode(), state);
        }

    }

}
