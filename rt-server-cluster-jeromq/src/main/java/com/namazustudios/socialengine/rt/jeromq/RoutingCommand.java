package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.remote.PackedUUID;
import javolution.io.Struct;

/**
 * Used to control the routes stored within a {@link RoutingTable}.
 */
public class RoutingCommand extends Struct {

    /**
     * The action to perform.
     */
    public final Enum32<Action> action = new Enum32<>(Action.values());

    /**
     * The destination {@link java.util.UUID} to control
     */
    public final PackedUUID destination = inner(new PackedUUID());

    /**
     * A list of actions which can be performed.
     */
    public enum Action {

        /**
         * Adds a connection to the multiplexer
         */
        OPEN,

        /**
         * Removes a connection from the multiplexer
         */
        CLOSE

    }

}
