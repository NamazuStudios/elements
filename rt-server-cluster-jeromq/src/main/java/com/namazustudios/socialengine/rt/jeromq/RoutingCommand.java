package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.remote.PackedUUID;
import javolution.io.Struct;

import java.util.UUID;

/**
 * Used to control the routes stored within a {@link InprocChannelTable}.
 */
public class RoutingCommand extends Struct {

    public static RoutingCommand buildRoutingCommand(
            final Action action,
            final String tcpAddress,
            final UUID inprocIdentifier
            ) {
        if (action == null) {
            throw new IllegalArgumentException("Action must not be null");
        }

        final RoutingCommand routingCommand = new RoutingCommand();

        routingCommand.action.set(action);

        if (tcpAddress != null) {
            routingCommand.tcpAddress.set(tcpAddress);
        }

        if (inprocIdentifier != null) {
            routingCommand.inprocIdentifier.set(inprocIdentifier);
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
     * The tcpAddress destination. This should be non-null if action is {@link Action#CONNECT_TCP} or
     * {@link Action#DISCONNECT_TCP}, and it will be ignored if the action is INPROC-related.
     *
     * TODO: split out the tcp routing commands into a separate struct to reduce inproc command transmission size.
     */
    public final UTF8String tcpAddress = new UTF8String(64);

    /**
     * The inprocIdentifier {@link java.util.UUID} to control. This should be non-null if action is
     * INPROC-related, and it will be ignored if the action is
     * {@link Action#CONNECT_TCP} or {@link Action#DISCONNECT_TCP}.
     */
    public final PackedUUID inprocIdentifier = inner(new PackedUUID());

    /**
     * A list of actions which can be performed.
     */
    public enum Action {

        /**
         * Unused for now.
         */
        NO_OP,

        /**
         * Establishes a DEALER connection to a ROUTER tcp socket at the given {@link RoutingCommand#tcpAddress}.
         */
        CONNECT_TCP,

        /**
         * Ends the DEALER connection to the ROUTER tcp socket at the given {@link RoutingCommand#tcpAddress}.
         */
        DISCONNECT_TCP,

        /**
         * Binds a ROUTER tcp socket with the given {@link RoutingCommand#tcpAddress}.
         */
        BIND_TCP,

        /**
         * Unbinds the ROUTER tcp socket with the given {@link RoutingCommand#tcpAddress}.
         */
        UNBIND_TCP,

        /**
         * Establishes a DEALER connection to a ROUTER inproc socket for the given {@link RoutingCommand#inprocIdentifier}.
         */
        CONNECT_INPROC,

        /**
         * Ends the DEALER connection from the ROUTER inproc socket for the given {@link RoutingCommand#inprocIdentifier}.
         */
        DISCONNECT_INPROC,

        /**
         * Binds a ROUTER inproc socket with the given {@link RoutingCommand#inprocIdentifier}.
         */
        BIND_INPROC,

        /**
         * Unbinds the ROUTER inproc socket for the given {@link RoutingCommand#inprocIdentifier}.
         */
        UNBIND_INPROC,

    }

}
