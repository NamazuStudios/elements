package com.namazustudios.socialengine.rt;

import java.util.UUID;

public interface InstanceResourceMonitorService {
    default void start(long refreshRateMillis) {}

    default void stop() {}

    /**
     *
     * TODO: maybe expose these metrics via http to hook up monitor dashboards (grafana etc.).
     *
     * @param instanceUuid
     * @return a non-negative load average if the monitor service has received a
     */
    double getMostRecentLoadAverage(final UUID instanceUuid);

    // TODO: not implemented for now
//    long getMostRecentInMemoryResourceCount(final NodeId nodeId);

    // TODO: set up strategy class and define these options in there, including round robin, random, by in-memory
    //  resource allocation count, etc.
    UUID getInstanceUuidByOptimalLoadAverage();

    UUID getRandomInstanceUuid();

    boolean isRunning();
}
