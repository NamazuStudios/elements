package dev.getelements.elements.sdk.cluster.remote.dto;

import dev.getelements.elements.sdk.cluster.id.InstanceId;

import static dev.getelements.elements.sdk.cluster.remote.dto.InstanceMetadata.InstanceState.WAITING;

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

    /**
     * The default {@link InstanceMetadata}.
     */
    public static final InstanceMetadata DEFAULT = new InstanceMetadata(
            InstanceId.NULL_INSTANCE_ID,
            0,
            0,
            0,
            WAITING
    );

    /**
     * Indicates the instance state.
     */
    public enum InstanceState {

        /**
         * Waiting for the instance to be ready and up.
         */
        WAITING,

        /**
         * Indicates that the instance is ready and accepting work.
         */
        ACCEPTING,

        /**
         * Hints that the instance is near capacity. This means that the instance will accept jobs, but is asking
         * clients not to dispatch new work. Functionally, it's identical to {@link #ACCEPTING}, but will soon hit an
         * upper limit.
         */
        NEAR_CAPACITY,

        /**
         * Indicates that the instance is not accepting jobs. The instance will process existing work, but will not
         * accept new work. Attempts to send any invocation. This indicates a temporary condition and the instance may
         * transition back down to {@link #NEAR_CAPACITY} or {@link #ACCEPTING} once jobs process.
         */
        AT_CAPACITY,

        /**
         * Indicates that the instance is not accepting jobs. The instance will process existing work, but will not
         * accept new work. This is a terminating state and will persist until all work is complete or as long as
         * reasonably possible
         */
        NOT_ACCEPTING_JOBS,

        /**
         * The instance is shut down and permanently offline.
         */
        TERMINATED

    }

}
