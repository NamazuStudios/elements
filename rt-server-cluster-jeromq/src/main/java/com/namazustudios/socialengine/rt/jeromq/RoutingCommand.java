package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.remote.PackedUUID;
import javolution.io.Struct;

/**
 * Used to control the routes stored within a {@link InprocChannelTable}.
 */
public class RoutingCommand extends Struct {

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
    public final UTF8String tcpAddress = new UTF8String(128);

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
         * Establishes a DEALER connection to a ROUTER tcp socket at the given {@link RoutingCommand#tcpAddress}.
         */
        CONNECT_TCP,

        /**
         * Ends the DEALER connection to the ROUTER tcp socket at the given {@link RoutingCommand#tcpAddress}.
         */
        DISCONNECT_TCP,

        /**
         * Binds a ROUTER inproc socket with the given {@link RoutingCommand#inprocIdentifier}.
         */
        BIND_INPROC,

        /**
         * Unbinds the ROUTER inproc socket for the given {@link RoutingCommand#inprocIdentifier}.
         */
        UNBIND_INPROC,

        /**
         * Establishes a DEALER connection to a ROUTER inproc socket for the given {@link RoutingCommand#inprocIdentifier}.
         */
        CONNECT_INPROC,

        /**
         * Ends the DEALER connection from the ROUTER inproc socket for the given {@link RoutingCommand#inprocIdentifier}.
         */
        DISCONNECT_INPROC,

    }

}
