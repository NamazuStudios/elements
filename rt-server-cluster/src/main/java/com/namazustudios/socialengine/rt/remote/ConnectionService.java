package com.namazustudios.socialengine.rt.remote;

import java.nio.ByteBuffer;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.ROUTING_COMMAND;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.NodeId;
import com.namazustudios.socialengine.rt.remote.RoutingCommand.Action;

import static com.namazustudios.socialengine.rt.remote.RoutingCommand.buildRoutingCommand;
import static com.namazustudios.socialengine.rt.remote.RoutingCommand.Action.*;
import static com.namazustudios.socialengine.rt.remote.RoutingCommand.Action.DISCONNECT_TCP;

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
        issueRoutingCommand(BIND_TCP, tcpAddress, null);
    }

    default void issueUnbindTcpCommand(final String tcpAddress) {
        issueRoutingCommand(UNBIND_TCP, tcpAddress, null);
    }

    default void issueConnectTcpCommand(final String tcpAddress) {
        issueRoutingCommand(CONNECT_TCP, tcpAddress, null);
    }

    default void issueDisconnectTcpCommand(final String tcpAddress) {
        issueRoutingCommand(DISCONNECT_TCP, tcpAddress, null);
    }

    default void issueBindInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(BIND_INPROC, tcpAddress, nodeId);
    }

    default void issueUnbindInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(UNBIND_INPROC, tcpAddress, nodeId);
    }

    default void issueConnectInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(CONNECT_INPROC, tcpAddress, nodeId);
    }

    default void issueDisconnectInprocCommand(final String tcpAddress, final NodeId nodeId) {
        issueRoutingCommand(DISCONNECT_INPROC, tcpAddress, nodeId);
    }

    default void issueRoutingCommand(final Action action, final String tcpAddress, final NodeId nodeId) {
        final RoutingCommand command = buildRoutingCommand(action, tcpAddress, nodeId);
        issueRoutingCommand(command);
    }

    default void issueRoutingCommand(final RoutingCommand command) {
        issueCommand(ROUTING_COMMAND, command.getByteBuffer());
    }

    /**
     * Issues a command over the control socket.
     *
     * @param commandType the type of command to send
     * @param byteBuffer the byte buffer to send
     */
    void issueCommand(final CommandType commandType, final ByteBuffer byteBuffer);

    boolean connectToBackend(final HostAndPort hostAndPort);
    boolean disconnectFromBackend(final HostAndPort hostAndPort);
}
