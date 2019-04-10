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

    public final UTF8String backendAddress = new UTF8String(128);

    /**
     * The inprocIdentifier {@link java.util.UUID} to control
     */
    public final PackedUUID inprocIdentifier = inner(new PackedUUID());

    /**
     * A list of actions which can be performed.
     */
    public enum Action {

        /**
         * Adds a backend (tcp) connection to the multiplexer
         */
        OPEN_BACKEND,

        /**
         * Removes a backend (tcp) connection from the multiplexer
         */
        CLOSE_BACKEND,

        /**
         * Adds an inproc connection to the multiplexer
         */
        OPEN_INPROC,

        /**
         * Removes an inproc connection from the multiplexer
         */
        CLOSE_INPROC,

    }

}
