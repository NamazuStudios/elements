package com.namazustudios.socialengine.rt;

import java.net.SocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This is the main class sed to manage the lifecycle of the server, drive the main threads, etc.
 *
 * Created by patricktwohig on 9/12/15.
 */
public interface ServerContainer {

    /**
     * Binds and runs the server.  This kicks off the main server thread, which then
     * will begin accepting requets.  The returned {@link Future} is used to determine
     * when the server is fully initiated.
     *
     * @param socketAddresses the {@link SocketAddress}, or addresses used for listening.
     */
    RunningInstance run(final SocketAddress ... socketAddresses);

    interface RunningInstance {

        /**
         * Waits for shutdown.  Note that this will wait indefinitely until the call to
         * {@link #shutdown()} is made.  This does nothing to change the state of the
         * currently running server.
         *
         * @throws InterruptedException
         */
        void waitForShutdown() throws InterruptedException;

        /**
         * Waits for shutdown with the given timeout and time unit.
         *
         * Note that this will wait until a call to {@link #shutdown()} is made and does
         * nothing to affect the state of the running server.
         *
         * @param timeout
         * @param timeUnit
         * @throws InterruptedException
         */
        void waitForShutdown(final long timeout, final TimeUnit timeUnit) throws InterruptedException;

        /**
         * Issues all the necessary signals to cleanly shut down the server.  Note that this call will
         * immediately return.  The server may not be
         */
        void shutdown();

    }

}
