package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.InstanceHostInfo;
import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.ProxyBuilder;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import org.zeromq.ZContext;

import java.util.function.Consumer;

class JeroMQInstanceConnection implements InstanceConnectionService.InstanceConnection {

    private final ZContext zContext;

    private final InstanceId instanceId;

    private final RemoteInvoker remoteInvoker;

    private final String internalControlAddress;

    private final InstanceHostInfo instanceHostInfo;

    private final InstanceMetadataContext instanceMetadataContext;

    private final Consumer<JeroMQInstanceConnection> onDisconnect;

    public JeroMQInstanceConnection(final InstanceId instanceId,
                                    final RemoteInvoker remoteInvoker,
                                    final ZContext zContext,
                                    final String internalControlAddress,
                                    final InstanceHostInfo instanceHostInfo,
                                    final Consumer<JeroMQInstanceConnection> onDisconnect) {
        this.zContext = zContext;
        this.instanceId = instanceId;
        this.onDisconnect = onDisconnect;
        this.remoteInvoker = remoteInvoker;
        this.instanceHostInfo = instanceHostInfo;
        this.internalControlAddress = internalControlAddress;
        this.instanceMetadataContext = new ProxyBuilder<>(InstanceMetadataContext.class)
            .dontProxyDefaultMethods()
            .withDefaultHashCodeAndEquals()
            .withHandlersForRemoteInvoker(remoteInvoker)
            .build();
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    public String openRouteToNode(NodeId nodeId) {
        try (ControlClient client = new JeroMQControlClient(zContext, internalControlAddress)) {
            final String address = client.openRouteToNode(nodeId, instanceHostInfo.getConnectAddress());
            return address;
        }
    }

    @Override
    public InstanceMetadataContext getInstanceMetadataContext() {
        return instanceMetadataContext;
    }

    @Override
    public void disconnect() {
        onDisconnect.accept(this);
    }

    /**
     * Gets the {@link RemoteInvoker} associated with this {@link JeroMQInstanceConnection}.
     *
     * @return the {@link RemoteInvoker}
     */
    public RemoteInvoker getRemoteInvoker() {
        return remoteInvoker;
    }

}
