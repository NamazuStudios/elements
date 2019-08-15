package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import javax.inject.Provider;

import static com.namazustudios.socialengine.rt.id.ApplicationId.forUniqueName;

public class NodeIdModule extends PrivateModule {

    private final Provider<NodeId> nodeIdProvider;

    public NodeIdModule(Provider<NodeId> nodeIdProvider) {
        this.nodeIdProvider = nodeIdProvider;
    }

    @Override
    protected void configure() {
        bind(NodeId.class).toProvider(nodeIdProvider);
        expose(NodeId.class);
    }

    /**
     * Supplies a {@link NodeId} for a master node using {@link ApplicationId#forUniqueName(String)} and further
     * constructing the using{@link NodeId#NodeId(InstanceId, ApplicationId)}
     *
     * @param instanceIdProvider the {@link Provider<InstanceId>}
     * @return the {@link NodeIdModule}
     */
    public static NodeIdModule forMasterNode(final Provider<InstanceId> instanceIdProvider) {
        return new NodeIdModule(() -> NodeId.forMasterNode(instanceIdProvider.get()));
    }

    /**
     * Supplies a {@link NodeId} for a node using {@link ApplicationId#forUniqueName(String)} and further constructing
     * the {@link NodeId} using{@link NodeId#NodeId(InstanceId, ApplicationId)}
     *
     * @param instanceIdProvider the {@link Provider<InstanceId>}
     */
    public static NodeIdModule forApplicationUniqueName(final Provider<InstanceId> instanceIdProvider,
                                                        final String applicationUniqueName) {
        return forApplication(instanceIdProvider, forUniqueName(applicationUniqueName));
    }

    /**
     * Supplies a {@link NodeId} for a master node using using{@link NodeId#NodeId(InstanceId, ApplicationId)}
     *
     * @param instanceIdProvider the {@link Provider<InstanceId>}
     */
    public static NodeIdModule forApplication(final Provider<InstanceId> instanceIdProvider,
                                              final ApplicationId applicationId) {
        return new NodeIdModule(() -> new NodeId(instanceIdProvider.get(), applicationId));
    }

}
