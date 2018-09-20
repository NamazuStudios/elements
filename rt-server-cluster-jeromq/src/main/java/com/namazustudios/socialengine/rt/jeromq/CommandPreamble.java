package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.remote.PackedUUID;
import javolution.io.Struct;

public class CommandPreamble extends Struct {

    /**
     * The command type
     */
    public final Enum32<CommandType> commandType= new Enum32<>(CommandType.values());

    /**
     * A list of command types.
     */
    public enum CommandType {

    /**
     * A RoutingCommand
     */
    ROUTING_COMMAND,

    /**
     * A StatusRequest
     */
    STATUS

    }

}
