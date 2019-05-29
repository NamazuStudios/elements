package com.namazustudios.socialengine.rt;

public interface ResourceAvailabilityMonitorService {
    default void start(double refreshRate) {}

    default void stop() {}

    double getMostRecentLoadAverage(final Object networkAddressAlias);

    long getMostRecentInMemoryResourceCount(final Object networkAddressAlias);

    // TODO: set up strategy class and define these options in there, including round robin, random, by in-memory
    //  resource allocation count, etc.
    Object getNetworkAddressAliasByOptimalLoadAverage();

    boolean isRunning();
}
