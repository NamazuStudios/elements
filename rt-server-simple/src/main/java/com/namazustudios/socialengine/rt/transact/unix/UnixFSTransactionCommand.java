package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;
import javolution.io.Struct;

/**
 * Enumerates all commands that can take place during the scope of a transaction.
 */
public class UnixFSTransactionCommand {

    public final Header header = new Header();

    public static class Header extends Struct {

        /**
         * The instruction to execute.
         */
        public final Enum16<Instruction> instruction = new Enum16<>(Instruction.values());

        /**
         * Indicates whether or not the instruction was processed.
         */
        private final Bool processed = new Bool();

        /**
         * Indicates how many bytes this command will occupy.
         */
        public final Unsigned32 lengthInBytes = new Unsigned32();

        /**
         * Represents the number of parameters passed to the command.
         */
        public final Unsigned32 parameterCount = new Unsigned32();

    }

    /**
     * Indicates the instruction.
     */
    enum Instruction {
        /**
         * Links path to a {@link ResourceId}.
         */
        UNLINK_FS_PATH
    }

}
