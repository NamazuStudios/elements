package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.remote.InstanceHostInfo;
import dev.getelements.elements.rt.InstanceMetadataContext;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.ControlClient;
import dev.getelements.elements.rt.remote.InstanceConnectionService;
import dev.getelements.elements.rt.remote.ProxyBuilder;
import dev.getelements.elements.rt.remote.RemoteInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.function.Consumer;

class JeroMQInstanceConnection implements InstanceConnectionService.InstanceConnection {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInstanceConnection.class);

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
        try (final ControlClient client = new JeroMQControlClient(zContext::shadow, internalControlAddress)) {
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

    @Override
    public String toString() {
        return "JeroMQInstanceConnection{" +
                "instanceId=" + instanceId +
                '}';
    }

}
