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

import static java.lang.Runtime.getRuntime;

public class JeroMQAsyncOperation implements AsyncOperation {

    private static final int THREAD_COUNT = getRuntime().availableProcessors() + 1;

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncOperation.class);

    private static final ScheduledExecutorService cancelTimer = Executors.newScheduledThreadPool(THREAD_COUNT);

    private final AsyncConnectionPool<ZContext, ZMQ.Socket> pool;

    private final AtomicReference<ConnectionState> state = new AtomicReference<>(State.CONNECTION_PENDING);

    public JeroMQAsyncOperation(AsyncConnectionPool<ZContext, ZMQ.Socket> pool) {
        this.pool = pool;
    }

    public void finish() {

    }

    @Override
    public void cancel() {
        state.getAndUpdate(current -> {
            switch (current.state) {
                case FINISHED:
                case CANCELED:
                case CONNECTION_PENDING:
                    return current.clear();
                case CONNECTION_ACQUIRED:
                    return current.cancel();
                default:
                    throw new IllegalStateException("Invalid state: " + current);
            }
        }).process();
    }

    @Override
    public void timeout(final long time, final TimeUnit timeUnit) {
        cancelTimer.schedule(this::cancel, time, timeUnit);
    }

    public boolean acquire(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
        return state.compareAndSet(State.CONNECTION_PENDING, State.CONNECTION_ACQUIRED);
    }

    public enum State {

        /**
         * Indicates that the operation is acquiring a connection from the connection pool.
         */
        CONNECTION_PENDING,

        /**
         * Indicates that the operation is acquiring a
         */
        CONNECTION_ACQUIRED,

        /**
         * Indicates that the operation is pending cancelation.
         */
        CANCEL_PENDING,

        /**
         * Indicates that the invocation has been canceled successfully.
         */
        CANCELED,

        /**
         * Indicates that the invocation has finished with or without errors.
         */
        FINISHED,

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

        private ConnectionState clear() {
            return new ConnectionState(state, null);
        }

        private ConnectionState update(final State state) {
            return new ConnectionState(state, connection);
        }

        public ConnectionState cancel() {
            return new ConnectionState(State.CANCELED, connection);
        }

        public void process() {

        }
                                    
    }

}
