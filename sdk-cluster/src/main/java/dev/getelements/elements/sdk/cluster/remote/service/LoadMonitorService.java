package dev.getelements.elements.sdk.cluster.remote.service;

import dev.getelements.elements.sdk.cluster.remote.annotation.RemoteService;

/**
 * Monitors the load of the local instance and reports information such as CPU load and Memory load.
 */
@RemoteService
public interface LoadMonitorService {

    /**
     * Gets a "quality" measurement of the instance.
     *
     * @return the quality of the instance
     */
    double getInstanceQuality();

}
