package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Represents the worker, hosing one or more nodes performing work.
 */
public interface Worker {

    /**
     * Used with {@link javax.inject.Named} to name an instance of {@link ExecutorService} which is a general purpose
     * pool of threads used for performing various tasks within the system.
     */
    String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.worker.executor";

    /**
     * Used with {@link javax.inject.Named} to name an instance of {@link java.util.concurrent.ScheduledExecutorService}
     * which is a general purpose pool of threads used for performing various tasks within the system.
     */
    String SCHEDULED_EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.scheduled.executor";

    /**
     * Gets a set of all active nodes.  This may change as new nodes may be introduced as well as removed.  The returned
     * {@link Set<NodeId>} shall be an instantaneous snapshot.  The returned set may be empty if no nodes are active.
     *
     * @return a {@link Set<NodeId>}
     */
    Set<NodeId> getActiveNodeIds();

}
