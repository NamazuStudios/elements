package com.namazustudios.socialengine.rt.jeromq;

import java.nio.ByteBuffer;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.ROUTING_COMMAND;
import com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.buildRoutingCommand;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.*;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.DISCONNECT_TCP;

public interface ConnectionService {

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

    default void issueBindInprocCommand(final String tcpAddress, final UUID inprocIdentifier) {
        issueRoutingCommand(BIND_INPROC, tcpAddress, inprocIdentifier);
    }

    default void issueUnbindInprocCommand(final String tcpAddress, final UUID inprocIdentifier) {
        issueRoutingCommand(UNBIND_INPROC, tcpAddress, inprocIdentifier);
    }

    default void issueConnectInprocCommand(final String tcpAddress, final UUID inprocIdentifier) {
        issueRoutingCommand(CONNECT_INPROC, tcpAddress, inprocIdentifier);
    }

    default void issueDisconnectInprocCommand(final String tcpAddress, final UUID inprocIdentifier) {
        issueRoutingCommand(DISCONNECT_INPROC, tcpAddress, inprocIdentifier);
    }

    default void issueRoutingCommand(final Action action, final String tcpAddress, final UUID inprocIdentifier) {
        final RoutingCommand command = buildRoutingCommand(action, tcpAddress, inprocIdentifier);
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
}
