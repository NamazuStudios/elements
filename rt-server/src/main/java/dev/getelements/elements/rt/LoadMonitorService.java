package dev.getelements.elements.rt;

/**
 * Monitors the load of the local instance and reports information such as CPU load and Memory load.
 */
public interface LoadMonitorService {

    /**
     * Starts the {@link LoadMonitorService}
     */
    default void start() {}

    /**
     * Stops the {@link LoadMonitorService}
     */
    default void stop() {}

    /**
     * Gets a "quality" measurement of the instance.  The lower the value the better the quality.  This can be derived
     * from weighted averages of CPU/Memory percentage.
     *
     * @return the quality of the instance
     */
    double getInstanceQuality();

}
