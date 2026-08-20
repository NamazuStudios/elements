package dev.getelements.elements.rt.remote.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;

import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.forUniqueName;
import static dev.getelements.elements.sdk.cluster.id.NodeId.forInstanceAndDeployment;

public class NodeIdModule extends PrivateModule {

    private final Provider<NodeId> nodeIdProvider;

    public NodeIdModule(final NodeId nodeId) {
        this(() -> nodeId);
    }

    public NodeIdModule(final Provider<NodeId> nodeIdProvider) {
        this.nodeIdProvider = nodeIdProvider;
    }

    @Override
    protected void configure() {
        bind(NodeId.class).toProvider(nodeIdProvider);
        expose(NodeId.class);
    }

    /**
     * Supplies a {@link NodeId} for a master node using {@link DeploymentId#forUniqueName(String)} and further
     * constructing the using{@link NodeId#forInstanceAndDeployment(InstanceId, DeploymentId)}
     *
     * @param instanceIdProvider the {@link Provider<InstanceId>}
     * @return the {@link NodeIdModule}
     */
    public static NodeIdModule forMasterNode(final Provider<InstanceId> instanceIdProvider) {
        return new NodeIdModule(() -> NodeId.forMasterNode(instanceIdProvider.get()));
    }

    /**
     * Supplies a {@link NodeId} for a node using {@link DeploymentId#forUniqueName(String)} and further constructing
     * the {@link NodeId} using{@link NodeId#forInstanceAndDeployment(InstanceId, DeploymentId)}
     *
     * @param instanceIdProvider the {@link Provider<InstanceId>}
     */
    public static NodeIdModule forDeploymentUniqueName(final Provider<InstanceId> instanceIdProvider,
                                                        final String deploymentUniqueName) {
        return forDeployment(instanceIdProvider, forUniqueName(deploymentUniqueName));
    }

    /**
     * Supplies a {@link NodeId} for a master node using using
     * {@link NodeId#forInstanceAndDeployment(InstanceId, DeploymentId)}
     *
     * @param instanceIdProvider the {@link Provider<InstanceId>}
     */
    public static NodeIdModule forDeployment(final Provider<InstanceId> instanceIdProvider,
                                              final DeploymentId deploymentId) {
        return new NodeIdModule(() -> forInstanceAndDeployment(instanceIdProvider.get(), deploymentId));
    }

}
