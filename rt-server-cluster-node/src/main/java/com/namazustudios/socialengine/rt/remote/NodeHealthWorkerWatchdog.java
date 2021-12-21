package com.namazustudios.socialengine.rt.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.namazustudios.socialengine.rt.remote.NodeState.UNHEALTHY;
import static java.util.stream.Collectors.toSet;

public class NodeHealthWorkerWatchdog implements WorkerWatchdog {

    private static final Logger logger = LoggerFactory.getLogger(NodeHealthWorkerWatchdog.class);

    @Override
    public void watch(final Worker worker) {

        final boolean unhealthy;

        try (var accessor = worker.accessWorkerState()) {
            unhealthy = accessor
                .getNodeSet()
                .stream()
                .anyMatch(node -> UNHEALTHY.equals(node.getState()));
        }

        if (unhealthy) restartUnhealthy(worker);

    }

    private void restartUnhealthy(final Worker worker) {
        try (var mutator = worker.beginMutation()) {

            final var unhealthy = mutator.getNodeSet()
                .stream()
                .filter(node -> UNHEALTHY.equals(node.getState()))
                .map(Node::getNodeId)
                .collect(toSet());

            unhealthy.forEach(node -> logger.error("Node {} is unhealthy. Restarting.", node.getNodeId()));
            mutator.restart(unhealthy);
            mutator.commit();

        }
    }

}
