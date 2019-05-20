package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

public class CommandPreamble extends Struct {

    public static CommandPreamble CommandPreambleFromBytes(final byte[] bytes) {
        final CommandPreamble commandPreamble = new CommandPreamble();
        commandPreamble.getByteBuffer().put(bytes);
        return commandPreamble;
    }

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

    }

}
