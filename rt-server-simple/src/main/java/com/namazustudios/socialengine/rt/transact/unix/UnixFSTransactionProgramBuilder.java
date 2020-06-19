package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase;
import javolution.io.Struct;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionCommand.Instruction.*;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase.CLEANUP;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase.COMMIT;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Used to compile a set of instructions into a {@link UnixFSTransactionProgram} used for committing the requested
 * transactions to disk. This does no checking of the underlying database, and assumes that the calling code has
 * properly completed all pre-checks while building the code.
 */
public class UnixFSTransactionProgramBuilder {

    private NodeId nodeId;

    private ByteBuffer byteBuffer;

    private UnixFSChecksumAlgorithm checksumAlgorithm = UnixFSChecksumAlgorithm.ADLER_32;

    private final Map<ExecutionPhase, List<CommandWriter>> operations = new EnumMap<>(ExecutionPhase.class);

    private final Map<ExecutionPhase, List<UnixFSTransactionCommand>> commands = new EnumMap<>(ExecutionPhase.class);

    /**
     * Specifies the {@link NodeId} to associate with the program. The {@link NodeId} is used to
     *
     * @param nodeId the {@link NodeId}
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder withNodeId(final NodeId nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    /**
     * Specifies the {@link ByteBuffer} which will hold the program's code.
     *
     * @param byteBuffer the {@link ByteBuffer} to hold the program's code.
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder withByteBuffer(final ByteBuffer byteBuffer) {
        requireNonNull(byteBuffer);
        this.byteBuffer = byteBuffer;
        clear();
        return this;
    }

    /**
     * Specifies the {@link UnixFSChecksumAlgorithm} to use to guarantee the program's integrity.
     *
     * @param checksumAlgorithm the {@link UnixFSChecksumAlgorithm} to use
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder withChecksumAlgorithm(final UnixFSChecksumAlgorithm checksumAlgorithm) {
        requireNonNull(checksumAlgorithm);
        this.checksumAlgorithm = checksumAlgorithm;
        clear();
        return this;
    }

    /**
     * Adds a step to the program to unlink a {@link java.nio.file.Path}. Must point to a file.
     *
     * @param executionPhase  the execution phase to use
     * @param fsPath the {@link java.nio.file.Path} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder unlinkFile(final ExecutionPhase executionPhase,
                                                      final java.nio.file.Path fsPath) {

        requireNonNull(executionPhase);
        requireNonNull(fsPath);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(UNLINK_FS_PATH)
                .addFSPathParameter(fsPath)
            .build(byteBuffer)));

        clear();
        return this;

    }

    /**
     * Adds a step to the program to unlink a {@link com.namazustudios.socialengine.rt.Path} from the revision store.
     *
     * @param executionPhase  the execution phase to use
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder unlinkResource(final ExecutionPhase executionPhase,
                                                          final com.namazustudios.socialengine.rt.Path rtPath) {

        requireNonNull(executionPhase);
        requireNonNull(rtPath);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(UNLINK_RT_PATH)
                .addRTPathParameter(rtPath)
            .build(byteBuffer)));

        clear();

        return this;

    }

    /**
     * Unlinks the {@link java.nio.file.Path} pointing to a specific {@link ResourceId}.
     *
     * @param executionPhase  the execution phase to use
     * @param fsPath the {@link java.nio.file.Path} to unlink
     * @param resourceId the {@link ResourceId} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder linkResourceFile(final ExecutionPhase executionPhase,
                                                            final java.nio.file.Path fsPath,
                                                            final ResourceId resourceId) {

        requireNonNull(executionPhase);
        requireNonNull(fsPath);
        requireNonNull(resourceId);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(LINK_RESOURCE_FILE_TO_RESOURCE_ID)
                .addFSPathParameter(fsPath)
                .addResourceIdParameter(resourceId)
            .build(byteBuffer)));

        clear();

        return this;
    }

    /**
     * Unlinks the {@link java.nio.file.Path} pointing to a specific {@link com.namazustudios.socialengine.rt.Path}.
     *
     * @param executionPhase  the execution phase to use
     * @param fsPath the {@link java.nio.file.Path} to unlink
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder linkResourceFile(final ExecutionPhase executionPhase,
                                                            final java.nio.file.Path fsPath,
                                                            final com.namazustudios.socialengine.rt.Path rtPath) {

        requireNonNull(executionPhase);
        requireNonNull(fsPath);
        requireNonNull(rtPath);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(LINK_RESOURCE_FILE_TO_RT_PATH)
                .addFSPathParameter(fsPath)
                .addRTPathParameter(rtPath)
            .build(byteBuffer)));

        clear();

        return this;

    }

    /**
     * Links a {@link ResourceId} to a specific {@link com.namazustudios.socialengine.rt.Path}.
     *
     * @param executionPhase  the execution phase to use
     * @param resourceId the {@link ResourceId} to link
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path} to link
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder linkResource(final ExecutionPhase executionPhase,
                                                        final ResourceId resourceId,
                                                        final com.namazustudios.socialengine.rt.Path rtPath) {

        requireNonNull(executionPhase);
        requireNonNull(resourceId);
        requireNonNull(rtPath);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(LINK_RESOURCE_TO_RT_PATH)
                .addResourceIdParameter(resourceId)
                .addRTPathParameter(rtPath)
            .build(byteBuffer)));

        clear();

        return this;

    }

    /**
     * Removes a {@link ResourceId} from the database.
     *
     * @param executionPhase  the execution phase to use
     * @param resourceId the {@link ResourceId} to remove
     *
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder removeResource(final ExecutionPhase executionPhase,
                                                          final ResourceId resourceId) {

        requireNonNull(executionPhase);
        requireNonNull(resourceId);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(REMOVE_RESOURCE)
                .addResourceIdParameter(resourceId)
            .build(byteBuffer)));

        clear();

        return this;

    }

    private void clear() {
        commands.clear();
    }

    private List<CommandWriter> getOperations(final ExecutionPhase executionPhase) {
        return operations.computeIfAbsent(executionPhase, k -> new ArrayList<>());
    }

    /**
     * Compiles this program, setting the header values as well as writing the bytecode to {@link ByteBuffer} containing
     * the program's instructions. This does clear the checksum header, however, it does does not calculate the
     * final checksum as that is part of the committing process.
     *
     * @return a freshly compiled instance of {@link UnixFSTransactionProgram}
     */
    public UnixFSTransactionProgram compile() {

        if (nodeId == null) throw new IllegalStateException("NodeId must be set.");
        if (byteBuffer == null) throw new IllegalStateException("Byte buffer must be set.");

        final int programPosition = byteBuffer.position();
        for (int i = 0; i < UnixFSTransactionProgram.Header.SIZE; ++i) byteBuffer.put((byte)0xFF);

        final UnixFSTransactionProgram program = new UnixFSTransactionProgram(byteBuffer, programPosition);
        program.header.algorithm.set(checksumAlgorithm);
        program.header.checksum.set(0);

        int programLength = 0;

        // Compiles all Phases
        programLength += compile(COMMIT, program, program.header.commitPos, program.header.commitLen);
        programLength += compile(CLEANUP, program, program.header.cleanupPos, program.header.cleanupLen);

        program.header.nodeId.set(nodeId);
        program.header.length.set(programLength);

        return program;

    }

    private int compile(final ExecutionPhase executionPhase,
                        final UnixFSTransactionProgram program,
                        final Struct.Unsigned32 pos,
                        final Struct.Unsigned32 len) {

        final int position = byteBuffer.position() - program.header.getByteBufferPosition();

        final List<UnixFSTransactionCommand> commands = getOperations(executionPhase)
                .stream()
                .map(writer -> writer.safeWrite(byteBuffer))
                .collect(toList());

        this.commands.put(executionPhase, commands);

        final int length = byteBuffer.position() - position;

        pos.set(position);
        len.set(length);

        return length;

    }

//    /**
//     * If a previous call to {@link #compile()} was made, this will generate an instance of
//     * {@link UnixFSTransactionProgramInterpreter} based on the {@link UnixFSTransactionCommand} instances that were
//     * compiled as part of building the {@link UnixFSTransactionProgram}. This is useful to avoid re-parsing the
//     * commands.
//     *
//     * @return an instance of {@link UnixFSTransactionProgramInterpreter}
//     */
//    public UnixFSTransactionProgramInterpreter interpreter() {
//        final List<UnixFSTransactionCommand> commits = commands.get(COMMIT);
//        final List<UnixFSTransactionCommand> cleanups = commands.get(CLEANUP);
//        if (commits == null || cleanups == null) throw new IllegalStateException("Program has not bee compiled.");
//        return new UnixFSTransactionProgramInterpreter(program, commits, cleanups);
//    }

    @FunctionalInterface
    private interface CommandWriter {

        UnixFSTransactionCommand write(ByteBuffer buffer);

        default UnixFSTransactionCommand safeWrite(final ByteBuffer byteBuffer) {
            try {
                return write(byteBuffer);
            } catch (BufferOverflowException ex) {
                throw new InternalException(ex);
            }
        }

    }

}
