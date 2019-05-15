package com.namazustudios.socialengine.rt.remote;

public interface ResourceAvailabilityMonitorService {

    default void start(double refreshRate) {}

    default void stop() {}

    double getMostRecentLoadAverage(final String address);

    long getMostRecentInMemoryResourceCount(final String address);

    String getOptimalLoadAverageAddress();

    String getOptimalInMemoryResourceCountAddress();

    boolean isRunning();

}
