package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionParameter.*;

/**
 * Enumerates all commands that can take place during the scope of a transaction.
 */
public class UnixFSTransactionCommand {

    final Header header = new Header();

    final ByteBuffer byteBuffer;

    /**
     * Constructs a {@link UnixFSTransactionCommand} from the supplied byte buffer and position.
     *
     * @param byteBuffer the {@link ByteBuffer}
     * @param position the position of the first byte of the header.
     */
    UnixFSTransactionCommand(final ByteBuffer byteBuffer, int position) {
        this.byteBuffer = byteBuffer.slice();
        header.setByteBuffer(byteBuffer, position);
    }

    /**
     * Gets the parameter at the supplied position. Throwing an instance of {@link UnixFSProgramCorruptionException} if there
     * is a problem extracting the parameter from the command.
     *
     * @param index
     * @return an instance of {@link UnixFSTransactionParameter}
     */
    public UnixFSTransactionParameter getParameterAt(final int index) {

        if (index > header.parameterCount.get()) {
            throw new UnixFSProgramCorruptionException("Parameter index out of bounds.");
        }

        return UnixFSTransactionParameter.fromCommand(index, this);

    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * USed to build an instance of {@link UnixFSTransactionCommand}. This configures the command header as well as each
     * additional parameter associated with the command.
     */
    public static class Builder {

        private Builder() {}

        private Phase phase;

        private Instruction instruction;

        private final List<ParameterWriter> parameterOperations = new LinkedList<>();

        /**
         * Specifies the {@link Phase} of the command.
         *
         * @param phase the {@link Phase}
         *
         * @return this instance
         */
        public Builder withPhase(final Phase phase) {
            this.phase = phase;
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

        public void build(final ByteBuffer byteBuffer) {

            // Counts the parameters and locks the position of the command to the current byte buffer position

            final int paramCount = parameterOperations.size();
            final int commandPosition = byteBuffer.position();
            for (int i = 0; i < Header.SIZE; ++i) byteBuffer.put((byte)0xFF);

            final UnixFSTransactionCommand command = new UnixFSTransactionCommand(byteBuffer, commandPosition);

            // Set all headers to the desired values.
            command.header.phase.set(phase);
            command.header.instruction.set(instruction);
            command.header.parameterCount.set((short)paramCount);

            // Allocate space for the parameter headers
            for (int i = 0; i < paramCount * UnixFSTransactionParameter.Header.SIZE; ++i) byteBuffer.put((byte)0xFF);

            // Writes all parameters to the byte buffer

            final ListIterator<ParameterWriter> listIterator = parameterOperations.listIterator();

            while (listIterator.hasNext()) {
                final int parameterIndex = listIterator.previousIndex();
                final ParameterWriter parameterWriter = listIterator.next();
                parameterWriter.write(command, parameterIndex);
            }

        }

    }

    public static class Header extends Struct {

        public static final int SIZE = new UnixFSTransactionProgram.Header().size();

        /**
         * The instruction to execute.
         */
        public final Enum16<Phase> phase = new Enum16<>(Phase.values());

        /**
         * The instruction to execute.
         */
        public final Enum16<Instruction> instruction = new Enum16<>(Instruction.values());

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
         * Unlinks a filesystem path {@link ResourceId}, typically implented using {@link Files#delete(Path)} or similar
         * functionality.
         */
        UNLINK_FS_PATH,

        /**
         * Unlinks a {@link Resource}
         */
        UNLINK_RESOURCE,

        /**
         *  Links a {@link Path} to a {@link ResourceId}
         */
        LINK_FS_PATH_TO_RESOURCE,

        /**
         * Links a {@link java.nio.file.Path} to a {@link Path}
         */
        LINK_FS_PATH_TO_RT_PATH,

        /**
         * Links a {@link java.nio.file.Path} to a {@link ResourceId}
         */
        LINK_FS_PATH_TO_RESOURCE_ID,

        /**
         * Links a {@link ResourceId} to a {@link Path}
         */
        LINK_RESOURCE_TO_RT_PATH

    }

    /**
     * Indicates the phase of the a
     */
    public enum Phase {

        /**
         * Happens in the commit phase.
         */
        COMMIT,

        /**
         * Happens in the cleanup phase.
         */
        CLEANUP

    }

    @FunctionalInterface
    private interface ParameterWriter {

        void write(UnixFSTransactionCommand command, int parameter);

    }

}
