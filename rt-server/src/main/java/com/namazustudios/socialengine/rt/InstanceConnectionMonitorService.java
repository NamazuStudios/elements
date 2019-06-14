package com.namazustudios.socialengine.rt;

public interface InstanceConnectionMonitorService extends Listenable<InstanceConnectionMonitorServiceListener> {
    default void start() {}
    default void stop() {}
}