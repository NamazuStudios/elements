package com.namazustudios.socialengine.rt.jeromq;

import javolution.io.Struct;

/**
 * Used to issue connect/disconnect commands.
 */
public class ConnectCommand extends Struct {

    /**
     * The action to perform.
     */
    public final Enum32<Action> action = new Enum32<>(Action.values());

    /**
     * The destination to connect/disconnect.
     */
    public final UTF8String connectAddress = new UTF8String(128);

    /**
     * A list of actions which can be performed.
     */
    public enum Action {

        /**
         * Connects to the given connectAddress
         */
        CONNECT,

        /**
         * Disconnects from the given connectAddress
         */
        DISCONNECT

    }

}
