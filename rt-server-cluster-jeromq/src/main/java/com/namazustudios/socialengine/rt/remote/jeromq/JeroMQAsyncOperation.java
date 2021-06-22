package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.Connection;
import com.namazustudios.socialengine.rt.exception.AsyncOperationCanceledException;
import com.namazustudios.socialengine.rt.remote.AsyncOperation;
import com.namazustudios.socialengine.rt.remote.InvocationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQAsyncOperation.State.*;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;

public class JeroMQAsyncOperation implements AsyncOperation {

    private static final int THREAD_COUNT = getRuntime().availableProcessors() + 1;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncOperation.class);

    private static final ScheduledExecutorService cancelTimer = Executors.newScheduledThreadPool(THREAD_COUNT);

    private final AtomicReference<ConnectionState> state = new AtomicReference<>(new ConnectionState());

    @Override
    public void cancel() {
        doCancel(() -> new AsyncOperationCanceledException("User canceled.").fillInStackTrace());
    }

    private void doCancel(Supplier<Throwable> errorSupplier) {
        state.updateAndGet(current -> {
            switch (current.state) {
                case CANCELED:
                case CANCELLATION_PENDING:
                case FINISHED:
                case FINISH_PENDING:
                    // These states indicate that the connection has already finished and no actions are necessary. The
                    // connection state will simply do nothing when it is processed and cancel will eventually happen
                    // if it has not already done so.
                    return current;
                case CONNECTION_PENDING:
                    // We haven't even gotten to acquiring a connection. When a connection is acquired, it will
                    // be the responsibility of the caller to recycle the connection before it has been acquired and
                    // the request will simply never be sent.
                    return current.update(CANCELED, errorSupplier.get());
                case CONNECTION_ACQUIRED:
                    // The connection was acquired and we are waiting on a response. If a cancellation happens at this
                    // state then the connection should be destroyed because we aren't able to recycle the connection.
                    return current.update(CANCELLATION_PENDING, errorSupplier.get());
                default:
                    // This is here for future proofing.
                    throw new IllegalStateException("Invalid state: " + current);
            }
        }).cancel();

    }

    @Override
    public void timeout(final long time, final TimeUnit timeUnit) {

        final var trace = logger.isDebugEnabled() ?
            new Throwable().fillInStackTrace().getStackTrace() :
            new StackTraceElement[] {};

        cancelTimer.schedule(() -> doCancel(() -> {
            final var msg = format("Timeout after %s %s", time, timeUnit);
            final var exception = new AsyncOperationCanceledException(msg);
            if (logger.isDebugEnabled()) exception.setStackTrace(trace);
            return exception;
        }), time, timeUnit);
    }

    /**
     * Finishes the cancellation.
     *
     * @return
     */
    public ConnectionState finishCancellation() {
        return state.updateAndGet(cs -> cs.state.equals(CANCELLATION_PENDING) ? cs.update(CANCELED) : cs);
    }

    /**
     * Called when the connection is acquired. If the operation is still in a suitable state, this will return true
     * incicating that the operation may proceed. This is only valid if the current state is
     * {@link State#CONNECTION_PENDING}. For any other state, this will return false indicating that the connection
     * has already finished or is in the process of being canceled.
     *
     * @return true if the operation may proceed.
     */
    public ConnectionState acquire(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
        return state.updateAndGet(cs -> cs.state.equals(CONNECTION_PENDING) ? cs.acquire(connection) : cs);
    }

    /**
     * Attempts to finish the operation by setting the state to {@link State#FINISH_PENDING}. This indicates that the
     * response is ready and the response w
     * ill be written shortly. If this transition is successful, then a subsequent
     * call to {@link #finish()} must happen to ensure the connection is properly recycled.
     */
    public ConnectionState requestFinish() {
        return state.updateAndGet(cs -> cs.state.equals(CONNECTION_ACQUIRED) ? cs.update(FINISH_PENDING) : cs);
    }

    /**
     * Finishes the operation and returns the underlying connection to the pool.
     *
     * @return the current {@link ConnectionState}
     */
    public ConnectionState finish() {
        return state.updateAndGet(current -> {
            switch (current.state) {
                case CONNECTION_PENDING:
                    logger.error("Called finish() from {}.", CONNECTION_PENDING);
                case FINISHED:
                case CANCELED:
                case CONNECTION_ACQUIRED:
                case CANCELLATION_PENDING:
                    return current;
                case FINISH_PENDING:
                    return current.update(FINISHED);
                default:
                    throw new IllegalStateException("Unexpected state: " + current.state);
            }
        });
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JeroMQAsyncOperation{");
        sb.append("state=").append(state);
        sb.append('}');
        return sb.toString();
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
         * Indicates that the operation has been canceled successfully. This is a terminal state. Once in this state
         * no other state changes may happen.
         */
        CANCELED,

        /**
         * Indicates that the operation is in the process of finishing, but has not yet finished.
         */
        FINISH_PENDING,

        /**
         * Indicates that the invocation has finished and the connection has been returned to the connection pool.
         */
        FINISHED

    }

    public class ConnectionState {

        private final State state;

        private final Throwable error;

        private final AsyncConnection<ZContext, ZMQ.Socket> connection;

        private ConnectionState() {
            this.error = null;
            this.connection = null;
            this.state = State.CONNECTION_PENDING;
        }

        private ConnectionState(final State state,
                                final Throwable error,
                                final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            this.error = error;
            this.state = state;
            this.connection = connection;
        }

        private ConnectionState update(final State state) {
            return new ConnectionState(state, error, connection);
        }

        private ConnectionState acquire(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            return new ConnectionState(CONNECTION_ACQUIRED, error, connection);
        }

        private ConnectionState update(final State state, final Throwable error) {
            return new ConnectionState(state, error, connection);
        }

        public State getState() {
            return state;
        }

        public Throwable getError() {
            return error;
        }

        public InvocationError getInvocationError() {

            final var th = getError();

            if (th == null) {
                return null;
            } else {
                final var ie = new InvocationError();
                ie.setThrowable(getError());
                return ie;
            }

        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ConnectionState{");
            sb.append("state=").append(state);
            sb.append(", error=").append(error);
            sb.append('}');
            return sb.toString();
        }

        private void cancel() {
            if (state != CANCELLATION_PENDING) {
                logger.debug("In state {}. Nothing to do.", state);
            } else if (connection == null) {
                logger.error("Connection null in state {}.", CANCELLATION_PENDING);
            } else {
                logger.debug("Closing socket from state {}.", CANCELLATION_PENDING);
                connection.signal(Connection::close);
            }
        }
    }

}
