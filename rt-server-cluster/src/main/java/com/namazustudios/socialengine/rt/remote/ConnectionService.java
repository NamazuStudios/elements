package com.namazustudios.socialengine.rt.remote;

import java.nio.ByteBuffer;

import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.INSTANCE_CONNECTION_COMMAND;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.ROUTING_COMMAND;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.NodeId;
import com.namazustudios.socialengine.rt.remote.RoutingCommand.Action;

import static com.namazustudios.socialengine.rt.remote.RoutingCommand.buildRoutingCommand;
import static com.namazustudios.socialengine.rt.remote.RoutingCommand.Action.*;

import static com.namazustudios.socialengine.rt.remote.InstanceConnectionCommand.buildInstanceConnectionCommand;

public interface ConnectionService {

    String getControlAddress();

    /**
     * Starts the service.
     */
    void start();

    /**
     * Stops the service.
     */
    void stop();

    default void issueBindTcpCommand(final String tcpAddress) {
        issueRoutingCommand(BIND_INVOKER, tcpAddress, null);
    }

    default void issueUnbindTcpCommand(final String tcpAddress) {
        issueRoutingCommand(UNBIND_INVOKER, tcpAddress, null);
    }

    default void issueBindInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(BIND_NODE, tcpAddress, nodeId);
    }

    default void issueUnbindInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(UNBIND_NODE, tcpAddress, nodeId);
    }

    default void issueConnectInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(CONNECT_NODE, tcpAddress, nodeId);
    }

    default void issueDisconnectInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(DISCONNECT_NODE, tcpAddress, nodeId);
    }

    default void issueRoutingCommand(final Action action, final String tcpAddress, final NodeId nodeId) {
        final RoutingCommand command = buildRoutingCommand(action, tcpAddress, nodeId);
        issueRoutingCommand(command);
    }

    default void issueRoutingCommand(final RoutingCommand command) {
        issueCommand(ROUTING_COMMAND, command.getByteBuffer());
    }

    default void issueConnectInstanceCommand(final String invokerTcpAddress, final String controlTcpAddress) {
        final InstanceConnectionCommand instanceConnectionCommand =
                buildInstanceConnectionCommand(InstanceConnectionCommand.Action.CONNECT, invokerTcpAddress, controlTcpAddress);
        issueCommand(INSTANCE_CONNECTION_COMMAND, instanceConnectionCommand.getByteBuffer());
    }

    default void issueDisconnectInstanceCommand(final String invokerTcpAddress, final String controlTcpAddress) {
        final InstanceConnectionCommand instanceConnectionCommand =
                buildInstanceConnectionCommand(InstanceConnectionCommand.Action.DISCONNECT, invokerTcpAddress, controlTcpAddress);
        issueCommand(INSTANCE_CONNECTION_COMMAND, instanceConnectionCommand.getByteBuffer());
    }

    /**
     * Issues a command over the control socket.
     *
     * @param commandType the type of command to send
     * @param byteBuffer the byte buffer to send
     */
    void issueCommand(final CommandType commandType, final ByteBuffer byteBuffer);

    boolean connectToInstance(final HostAndPort connectHostAndPort, final HostAndPort controlHostAndPort);
    boolean disconnectFromInstance(final HostAndPort invokerHostAndPort, final HostAndPort controlHostAndPort);
}
