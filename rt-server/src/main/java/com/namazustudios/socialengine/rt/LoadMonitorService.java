package com.namazustudios.socialengine.rt;

public interface LoadMonitorService extends AutoCloseable {

    default void start() {}

    default void stop() {}

    double getLoadAverage();

    boolean isRunning();

}
