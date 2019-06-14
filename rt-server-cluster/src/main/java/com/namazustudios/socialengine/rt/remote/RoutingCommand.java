package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.NodeId;
import com.namazustudios.socialengine.rt.remote.PackedUUID;
import javolution.io.Struct;

import java.util.UUID;

/**
 * Used to control the routes stored.
 *
 * TODO: maybe split out invoker routing command vs node routing command
 */
public class RoutingCommand extends Struct {
    public static RoutingCommand buildRoutingCommand(
        final Action action,
        final String tcpAddress
    ) {
        return buildRoutingCommand(action, tcpAddress, null);
    }

    public static RoutingCommand buildRoutingCommand(
        final Action action,
        final NodeId nodeId
    ) {
        return buildRoutingCommand(action, null, nodeId);
    }

    public static RoutingCommand buildRoutingCommand(
            final Action action,
            final String tcpAddress,
            final NodeId nodeId
            ) {
        if (action == null) {
            throw new IllegalArgumentException("Action must not be null");
        }

        final RoutingCommand routingCommand = new RoutingCommand();

        routingCommand.action.set(action);

        if (tcpAddress != null) {
            routingCommand.tcpAddress.set(tcpAddress);
        }

        if (nodeId != null) {
            if (nodeId.getInstanceUuid() != null) {
                routingCommand.instanceUuid.set(nodeId.getInstanceUuid());
            }

            if (nodeId.getApplicationUuid() != null) {
                routingCommand.applicationUuid.set(nodeId.getApplicationUuid());
            }
        }

        return routingCommand;
    }

    public static RoutingCommand RoutingCommandFromBytes(final byte[] bytes) {
        final RoutingCommand routingCommand = new RoutingCommand();
        routingCommand.getByteBuffer().put(bytes);
        return routingCommand;
    }

    /**
     * The action to perform.
     */
    public final Enum32<Action> action = new Enum32<>(Action.values());

    /**
     * The tcpAddress destination. This should be non-null if action is {@link Action#BIND_INVOKER}, and it will be
     * ignored otherwise.
     *
     * TODO: split out the tcp routing commands into a separate struct to reduce inproc command transmission size.
     */
    public final UTF8String tcpAddress = new UTF8String(64);

    /**
     * The instance uuid of a Node to control. This should be non-null if the action is Node related, and it will be
     * ignored otherwise.
     */
    public final PackedUUID instanceUuid = inner(new PackedUUID());

    /**
     * The application uuid of a Node to control. This should be non-null if the action is Node related, and it will be
     * ignored otherwise.
     */
    public final PackedUUID applicationUuid = inner(new PackedUUID());

    /**
     * A list of actions which can be performed.
     */
    public enum Action {

        /**
         * Unused for now.
         */
        NO_OP,

        /**
         * Binds a ROUTER tcp socket with the given {@link RoutingCommand#tcpAddress} to begin receiving invoker payloads.
         */
        BIND_INVOKER,

        /**
         * Unbinds the ROUTER tcp socket with the given {@link RoutingCommand#tcpAddress} to stop receiving invoker payloads.
         */
        UNBIND_INVOKER,

        /**
         * Establishes a DEALER connection to a ROUTER inproc socket.
         */
        CONNECT_NODE,

        /**
         * Ends the DEALER connection from the ROUTER inproc socket.
         */
        DISCONNECT_NODE,

        /**
         * Binds a ROUTER inproc socket.
         */
        BIND_NODE,

        /**
         * Unbinds the ROUTER inproc socket.
         */
        UNBIND_NODE,

    }
}
