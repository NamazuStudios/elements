package dev.getelements.elements.test;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.Worker;

/**
 * A container for a client {@link Instance}.
 */
public interface EmbeddedWorkerInstanceContainer extends EmbeddedInstanceContainer {

    /**
     * Gets the underlying {@link Worker}.
     *
     * @return the worker
     */
    Worker getWorker();

    /**
     * Gets the worker's bind address.
     *
     * @return the bind address
     */
    String getBindAddress();

    /**
     * Enables the default HTTP client for the {@link Worker} contained in this container.
     * s
     * @return this instance
     */
    EmbeddedWorkerInstanceContainer withDefaultHttpClient();

    /**
     * Returns the default Worker IoC resolver. The definition of the default worker is implementation specific.
     * Typically, this is the fist configured {@link DeploymentId}
     *
     * @return the default worker IoC resolver.
     */
    ServiceLocator getIocResolver();

    /**
     * Gets the {@link ServiceLocator} for the {@link Node} associated with the supplied {@link NodeId}
     *
     * @param nodeId
     * @return the {@link NodeId}
     */
    ServiceLocator getIocResolver(NodeId nodeId);

    /**
     * Gets the {@link ServiceLocator} for the {@link Node} associated with the supplied {@link NodeId}
     *
     * @param deploymentId the {@link DeploymentId} of the application to fetch
     * @return the {@link NodeId}
     */
    default ServiceLocator getIocResolver(final DeploymentId deploymentId) {
        final var nodeId = NodeId.forInstanceAndDeployment(getInstanceId(), deploymentId);
        return getIocResolver(nodeId);
    }

    /**
     * Fetches the default {@link NodeId} installed in this container. The definition of the default worker is
     * implementation specific. Typically, this is the fist configured {@link DeploymentId}.
     *
     * @return the {@link NodeId}
     */
    default NodeId getNodeId() {
        return getIocResolver().getInstance(NodeId.class);
    }

    /**
     * Fetches the default {@link DeploymentId} installed in this container. The definition of the default worker is
     * implementation specific. Typically, this is the fist configured {@link DeploymentId}.
     *
     * @return the {@link NodeId}
     */
    default DeploymentId getDeploymentId() {
        return getNodeId().getDeploymentId();
    }

}
