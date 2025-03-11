package dev.getelements.elements.rt.transact.unix;

import javolution.io.Struct;

public class UnixFSTransactionCommandHeader extends Struct {

    public static final int SIZE = new UnixFSTransactionCommandHeader().size();

    /**
     * The instruction to execute.
     */
    public final Enum16<UnixFSTransactionProgramExecutionPhase> phase = new Enum16<>(UnixFSTransactionProgramExecutionPhase.values());

    /**
     * The instruction to execute.
     */
    public final Enum16<UnixFSTransactionCommandInstruction> instruction = new Enum16<>(UnixFSTransactionCommandInstruction.values());

    /**
     * The length of the command, in bytes.
     */
    public final Unsigned32 length = new Unsigned32();

    /**
     * Represents the number of parameters passed to the command.
     */
    public final Unsigned8 parameterCount = new Unsigned8();

}
