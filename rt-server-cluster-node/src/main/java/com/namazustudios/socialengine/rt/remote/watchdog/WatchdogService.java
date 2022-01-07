package com.namazustudios.socialengine.rt.remote.watchdog;

/**
 * Times and executes watchdogs.
 */
public interface WatchdogService {

    /**
     * Starts monitoring.
     */
    void start();

    /**
     * Stops monitoring.
     */
    void stop();

}
