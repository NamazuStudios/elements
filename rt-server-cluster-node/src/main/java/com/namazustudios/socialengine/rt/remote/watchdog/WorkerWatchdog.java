package com.namazustudios.socialengine.rt.remote.watchdog;

import com.namazustudios.socialengine.rt.remote.Worker;
import org.slf4j.Logger;

/**
 * Represents a watchdog service which is designed to perform operations against the {@link Worker} on a regular basis,
 * ensuring things like health checks and other critical operations. There may be multiple {@link WorkerWatchdog}
 * instances for many different purposes and to avoid unnecessary concurrencty each {@link WorkerWatchdog} must define
 * it's own rules and perocess them upon request.
 */
public interface WorkerWatchdog {

    /**
     * Starts this {@link WorkerWatchdog}.
     */
    default void start() {}

    /**
     * Stops this {@link WorkerWatchdog}.
     */
    default void stop() {}

    /**
     * Executes the rules for this {@link WorkerWatchdog}
     * @param worker the {@link Worker} against which to operate
     */
    void watch(Logger logger, Worker worker);

}
