package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionParameter.*;

/**
 * Enumerates all commands that can take place during the scope of a transaction.
 */
public class UnixFSTransactionCommand {

    final Header header;

    private UnixFSTransactionCommand(final Header header) {
        this.header = header;
    }

    /**
     * Gets the parameter at the supplied position. Throwing an instance of {@link UnixFSProgramCorruptionException} if there
     * is a problem extracting the parameter from the command.
     *
     * @param index
     * @return an instance of {@link UnixFSTransactionParameter}
     */
    public UnixFSTransactionParameter getParameterAt(final int index) {
        return UnixFSTransactionParameter.fromCommand(this, index);
    }

    /**
     * Gets a {@link Builder} used to construct an instance of {@link UnixFSTransactionCommand} command.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Reads a new {@link UnixFSTransactionCommand} from the supplied {@link ByteBuffer}. This will read until the end
     * of the {@link ByteBuffer}
     *
     * @param byteBuffer the {@link ByteBuffer} to read from.
     */
    public static UnixFSTransactionCommand from(final ByteBuffer byteBuffer) {

        final ByteBuffer duplicate = byteBuffer.duplicate();

        final Header header = new Header();
        final int position = duplicate.position();
        header.setByteBuffer(duplicate, position);

        final long lLength = header.length.get();

        if (lLength > Integer.MAX_VALUE || lLength < 0) {
            throw new UnixFSProgramCorruptionException("Invalid command length: " + lLength);
        }

        duplicate.limit(position + (int) lLength);
        byteBuffer.position(position + (int) lLength);

        final ByteBuffer slice = duplicate.slice().asReadOnlyBuffer();
        header.setByteBuffer(slice, 0);

        return new UnixFSTransactionCommand(header);

    }

    /**
     * USed to build an instance of {@link UnixFSTransactionCommand}. This configures the command header as well as each
     * additional parameter associated with the command.
     */
    public static class Builder {

        private Builder() {}

        private UnixFSTransactionProgram.ExecutionPhase executionPhase;

        private Instruction instruction;

        private final List<ParameterWriter> parameterOperations = new LinkedList<>();

        /**
         * Specifies the {@link UnixFSTransactionProgram.ExecutionPhase} of the command.
         *
         * @param executionPhase the {@link UnixFSTransactionProgram.ExecutionPhase}
         *
         * @return this instance
         */
        public Builder withPhase(final UnixFSTransactionProgram.ExecutionPhase executionPhase) {
            this.executionPhase = executionPhase;
            return this;
        }

        /**
         * Specifies the {@link Instruction} to execute.
         *
         * @param instruction the {@link Instruction}
         * @return
         */
        public Builder withInstruction(final Instruction instruction) {
            this.instruction = instruction;
            return this;
        }

        /**
         * Appends a {@link java.nio.file.Path} parameter.
         *
         * @param path the {@link java.nio.file.Path} parameter
         *
         * @return this instance
         */
        public Builder addFSPathParameter(final java.nio.file.Path path) {
            if (parameterOperations.size() >= Short.MAX_VALUE) throw new InternalException("Exceeded parameter count");
            parameterOperations.add((command, param) -> appendFSPath(command, param, path));
            return this;
        }

        /**
         * Appends a {@link com.namazustudios.socialengine.rt.Path} parameter.
         *
         * @param path the {@link com.namazustudios.socialengine.rt.Path} parameter
         *
         * @return this instance
         */
        public Builder addRTPathParameter(final com.namazustudios.socialengine.rt.Path path) {
            if (parameterOperations.size() >= Short.MAX_VALUE) throw new InternalException("Exceeded parameter count");
            parameterOperations.add((command, param) -> appendRTPath(command, param, path));
            return this;
        }

        /**
         * Appends a {@link ResourceId} parameter.
         *
         * @param resourceId the {@link ResourceId} parameter
         *
         * @return this instance
         */
        public Builder addResourceIdParameter(final ResourceId resourceId) {
            if (parameterOperations.size() >= Short.MAX_VALUE) throw new InternalException("Exceeded parameter count");
            parameterOperations.add((command, param) -> appendResourceId(command, param, resourceId));
            return this;
        }

        /**
         * Builds the {@link UnixFSTransactionCommand} by appending it and its commands to the supplied
         * {@link ByteBuffer}.
         *
         * @param byteBuffer the {@link ByteBuffer} which will receive the {@link UnixFSTransactionCommand}
         */
        public UnixFSTransactionCommand build(final ByteBuffer byteBuffer) {

            final ByteBuffer duplicate = byteBuffer.duplicate();
            duplicate.mark();

            // Counts the parameters and locks the position of the command to the current byte buffer position

            final int paramCount = parameterOperations.size();
            final int commandPosition = duplicate.position();

            // Fills the header bytes full of place holder data.
            for (int i = 0; i < Header.SIZE; ++i) duplicate.put((byte)0xFF);

            // Creates the header and populates
            final Header header = new Header();

            // Set all headers to the desired values, overwriting previous clearing of the buffer.
            header.phase.set(executionPhase);
            header.instruction.set(instruction);
            header.parameterCount.set((short)paramCount);

            // Allocate space for the parameter headers
            for (int i = 0; i < paramCount * UnixFSTransactionParameter.Header.SIZE; ++i) duplicate.put((byte)0xFF);

            // Writes all parameters to the byte buffer

            final ListIterator<ParameterWriter> listIterator = parameterOperations.listIterator();

            while (listIterator.hasNext()) {
                final int parameterIndex = listIterator.previousIndex();
                final ParameterWriter parameterWriter = listIterator.next();
                parameterWriter.write(header, parameterIndex);
            }

            final int commandLength = byteBuffer.position() - commandPosition;
            header.length.set(commandLength);

            duplicate.reset();
            duplicate.limit(commandPosition + commandLength);

            final ByteBuffer slice = duplicate.slice().asReadOnlyBuffer();
            header.setByteBuffer(slice, 0);

            return new UnixFSTransactionCommand(header);

        }

    }

    public static class Header extends Struct {

        public static final int SIZE = new UnixFSTransactionProgram.Header().size();

        /**
         * The instruction to execute.
         */
        public final Enum16<UnixFSTransactionProgram.ExecutionPhase> phase = new Enum16<>(UnixFSTransactionProgram.ExecutionPhase.values());

        /**
         * The instruction to execute.
         */
        public final Enum16<Instruction> instruction = new Enum16<>(Instruction.values());

        /**
         * The length of the command, in bytes.
         */
        public final Unsigned32 length = new Unsigned32();

        /**
         * Represents the number of parameters passed to the command.
         */
        public final Unsigned8 parameterCount = new Unsigned8();

    }

    /**
     * Indicates the instruction.
     */
    enum Instruction {

        /**
         * No-op.
         */
        NOOP,

        /**
         * Unlinks a filesystem path {@link ResourceId}, typically implented using
         * {@link Files#delete(java.nio.file.Path)} or similar
         * functionality.
         */
        UNLINK_FS_PATH,

        /**
         * Unlinks a {@link ResourceId} from a {@link com.namazustudios.socialengine.rt.Path}
         */
        UNLINK_RT_PATH,

        /**
         * Removes a {@link Resource} with a supplied {@link ResourceId}
         */
        REMOVE_RESOURCE,

        /**
         * Updates an existing {@link Resource to a new version. This accepts botht he resource file to update as well
         * as the {@link ResourceId}.
         */
        UPDATE_RESOURCE,

        /**
         * Links a {@link java.nio.file.Path} to a {@link ResourceId}. Specifically, this would be used to link a
         * temporary file at the supplied {@link java.nio.file.Path} to a {@link ResourceId}. The existing resource must
         * not exist as this operation only makes sense for the first time a ResourceId is introduced.
         */
        LINK_NEW_RESOURCE,

        /**
         * Links a {@link ResourceId} to a {@link com.namazustudios.socialengine.rt.Path}
         */
        LINK_RESOURCE_TO_RT_PATH,

    }

    @FunctionalInterface
    private interface ParameterWriter {

        void write(UnixFSTransactionCommand.Header header, int parameter);

    }

}
