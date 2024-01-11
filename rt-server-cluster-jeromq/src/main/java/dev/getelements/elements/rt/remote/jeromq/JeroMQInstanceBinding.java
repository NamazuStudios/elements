package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.ControlClient;
import dev.getelements.elements.rt.remote.InstanceConnectionService.InstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.Objects;

public class JeroMQInstanceBinding implements InstanceBinding {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQInstanceBinding.class);

    private final ZContext zContext;

    private final NodeId nodeId;

    private final String instanceConnectAddress;

    private final JeroMQSecurity jeroMQSecurity;

    private final String nodeBindAddress;

    public JeroMQInstanceBinding(final ZContext zContext,
                                 final NodeId nodeId,
                                 final String instanceConnectAddress,
                                 final JeroMQSecurity jeroMQSecurity,
                                 final String nodeBindAddress) {
        this.zContext = zContext;
        this.nodeId = nodeId;
        this.instanceConnectAddress = instanceConnectAddress;
        this.jeroMQSecurity = jeroMQSecurity;
        this.nodeBindAddress = nodeBindAddress;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public String getBindAddress() {
        return nodeBindAddress;
    }

    @Override
    public void close() {
        try (final ControlClient client = new JeroMQControlClient(zContext, instanceConnectAddress, jeroMQSecurity)) {
            client.closeBinding(nodeId);
        } catch (Exception ex) {
            logger.warn("Caught exception closing binding.", ex);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JeroMQInstanceBinding)) return false;
        JeroMQInstanceBinding that = (JeroMQInstanceBinding) o;
        return nodeId.equals(that.nodeId) &&
            instanceConnectAddress.equals(that.instanceConnectAddress) &&
            nodeBindAddress.equals(that.nodeBindAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, instanceConnectAddress, nodeBindAddress);
    }

}
