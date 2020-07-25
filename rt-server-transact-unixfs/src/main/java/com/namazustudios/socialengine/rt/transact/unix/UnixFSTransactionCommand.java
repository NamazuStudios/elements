package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionParameter.*;

/**
 * Enumerates all commands that can take place during the scope of a transaction.
 */
public class UnixFSTransactionCommand {

    final UnixFSTransactionCommandHeader header;

    private UnixFSTransactionCommand(final UnixFSTransactionCommandHeader header) {
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

        final UnixFSTransactionCommandHeader header = new UnixFSTransactionCommandHeader();
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

        private UnixFSTransactionProgramExecutionPhase executionPhase;

        private UnixFSTransactionCommandInstruction instruction;

        private final List<ParameterWriter> parameterOperations = new LinkedList<>();

        /**
         * Specifies the {@link UnixFSTransactionProgramExecutionPhase} of the command.
         *
         * @param executionPhase the {@link UnixFSTransactionProgramExecutionPhase}
         *
         * @return this instance
         */
        public Builder withPhase(final UnixFSTransactionProgramExecutionPhase executionPhase) {
            this.executionPhase = executionPhase;
            return this;
        }

        /**
         * Specifies the {@link UnixFSTransactionCommandInstruction} to execute.
         *
         * @param instruction the {@link UnixFSTransactionCommandInstruction}
         * @return
         */
        public Builder withInstruction(final UnixFSTransactionCommandInstruction instruction) {
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
            for (int i = 0; i < UnixFSTransactionCommandHeader.SIZE; ++i) duplicate.put((byte)0xFF);

            // Creates the header and populates
            final UnixFSTransactionCommandHeader header = new UnixFSTransactionCommandHeader();

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

    @FunctionalInterface
    private interface ParameterWriter {

        /**
         * Writes the command to the
         * @param header
         * @param parameter
         */
        void write(UnixFSTransactionCommandHeader header, int parameter);

    }

}
