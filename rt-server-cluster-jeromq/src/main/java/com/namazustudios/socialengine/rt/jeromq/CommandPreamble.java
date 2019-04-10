package com.namazustudios.socialengine.rt.jeromq;

import javolution.io.Struct;

public class CommandPreamble extends Struct {

    /**
     * The command type
     */
    public final Enum32<CommandType> commandType = new Enum32<>(CommandType.values());

    /**
     * A list of command types.
     */
    public enum CommandType {

        /**
         * A RoutingCommand
         */
        ROUTING_COMMAND,

        /**
         * A RoutingCommand acknowledgement
         */
        ROUTING_COMMAND_ACK,

        /**
         * A StatusRequest
         */
        STATUS_REQUEST,

        /**
         * A StatusResponse
         */
        STATUS_RESPONSE,

        /**
         * A ConnectCommand
         */
        CONNECT_COMMAND,

        /**
         * A ConnectCommand acknowledgement
         */
        CONNECT_COMMAND_ACK,

    }

}
