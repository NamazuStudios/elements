package dev.getelements.elements.sdk.cluster.service;

import dev.getelements.elements.sdk.cluster.annotation.RemoteService;
import dev.getelements.elements.sdk.cluster.annotation.RemotelyInvokable;

/**
 * Monitors the load of the local instance and reports information such as CPU load and Memory load.
 */
@RemoteService
public interface LoadMonitorService {

    /**
     * Gets a "quality" measurement of the instance.  The lower the value the better the quality.  This can be derived
     * from weighted averages of CPU/Memory percentage.
     *
     * @return the quality of the instance
     */
    double getInstanceQuality();

}
