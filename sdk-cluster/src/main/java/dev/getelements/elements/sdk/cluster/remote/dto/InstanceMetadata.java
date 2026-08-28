package dev.getelements.elements.sdk.cluster.remote.dto;

import dev.getelements.elements.sdk.cluster.id.InstanceId;

/**
 * Reports the instance metadata used in load balancing.
 *
 * @param instanceId the instance id used for routing purposes
 * @param quality the self-reported quality of the instance between 0 and 1
 * @param cpuUsagePercentage the absolute CPU usage expressed as total CPU usage across all cores
 * @param memoryUsagePercentage the absolute memory usage expressed as the total memory consumed
 * @param state the state of the instance
 */
public record InstanceMetadata(
        InstanceId instanceId,
        double quality,
        double cpuUsagePercentage,
        double memoryUsagePercentage,
        InstanceState state) {

    public enum InstanceState {
        READY,
        NOT_ACCEPTING_JOBS
    }

}
