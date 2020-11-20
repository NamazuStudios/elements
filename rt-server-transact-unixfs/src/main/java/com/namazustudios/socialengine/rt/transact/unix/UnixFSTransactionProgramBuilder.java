package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import javolution.io.Struct;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionCommandInstruction.*;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.CLEANUP;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.COMMIT;
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

    private UnixFSRevision<?> revision;

    private final Map<UnixFSTransactionProgramExecutionPhase, List<CommandWriter>> operations = new EnumMap<>(UnixFSTransactionProgramExecutionPhase.class);

    private final Map<UnixFSTransactionProgramExecutionPhase, List<UnixFSTransactionCommand>> commands = new EnumMap<>(UnixFSTransactionProgramExecutionPhase.class);

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
    public UnixFSTransactionProgramBuilder unlinkFile(final UnixFSTransactionProgramExecutionPhase executionPhase,
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
    public UnixFSTransactionProgramBuilder unlinkResource(final UnixFSTransactionProgramExecutionPhase executionPhase,
                                                          final ResourceId resourceId,
                                                          final com.namazustudios.socialengine.rt.Path rtPath) {

        requireNonNull(executionPhase);
        requireNonNull(rtPath);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(UNLINK_RT_PATH)
                .addResourceIdParameter(resourceId)
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
    public UnixFSTransactionProgramBuilder linkNewResource(final UnixFSTransactionProgramExecutionPhase executionPhase,
                                                           final java.nio.file.Path fsPath,
                                                           final ResourceId resourceId) {

        requireNonNull(executionPhase);
        requireNonNull(fsPath);
        requireNonNull(resourceId);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(LINK_NEW_RESOURCE)
                .addFSPathParameter(fsPath)
                .addResourceIdParameter(resourceId)
            .build(byteBuffer)));

        clear();

        return this;
    }


    public UnixFSTransactionProgramBuilder updateResource(final UnixFSTransactionProgramExecutionPhase executionPhase,
                                                          final java.nio.file.Path fsPath,
                                                          final ResourceId resourceId) {

        requireNonNull(fsPath);
        requireNonNull(resourceId);
        requireNonNull(executionPhase);

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(UPDATE_RESOURCE)
                .addFSPathParameter(fsPath)
                .addResourceIdParameter(resourceId)
            .build(byteBuffer)));

        return this;
    }

    /**
     * Adds the supplied {@link Path} to the data store.
     *
     * @param executionPhase  the execution phase to use
     * @param path the {@link Path} to add
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder addPath(final UnixFSTransactionProgramExecutionPhase executionPhase,
                                                   final Path path) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(path, "path");

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(ADD_PATH)
                .addRTPathParameter(path)
                .build(byteBuffer)));

        clear();

        return this;

    }

    /**
     * Adds the supplied {@link ResourceId} to the data store.
     *
     * @param executionPhase  the execution phase to use
     * @param resourceId the {@link ResourceId} to add
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder addResourceId(final UnixFSTransactionProgramExecutionPhase executionPhase,
                                                        final ResourceId resourceId) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(resourceId, "resourceId");

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
            .withPhase(executionPhase)
            .withInstruction(ADD_RESOURCE_ID)
            .addResourceIdParameter(resourceId)
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
    public UnixFSTransactionProgramBuilder linkResource(final UnixFSTransactionProgramExecutionPhase executionPhase,
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
    public UnixFSTransactionProgramBuilder removeResource(final UnixFSTransactionProgramExecutionPhase executionPhase,
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

    /**
     * Sets the desired {@link Revision<?>} of this particular transaction program. Once executed, the database will be
     * set to this {@link Revision<?>}
     *
     * @param revision the {@link Revision<?>} to set
     * @return the this instance
     */
    public UnixFSTransactionProgramBuilder revision(final UnixFSRevision<?> revision) {
        this.revision = revision;
        clear();
        return this;
    }

    private void clear() {
        commands.clear();
    }

    private List<CommandWriter> getOperations(final UnixFSTransactionProgramExecutionPhase executionPhase) {
        return operations.computeIfAbsent(executionPhase, k -> new ArrayList<>());
    }

    /**
     * Compiles this program, setting the header values as well as writing the bytecode to {@link ByteBuffer} containing
     * the program's instructions. This does clear the checksum header, however, it does does not calculate the
     * final checksum as that is part of the committing process.
     *
     * @return a freshly compiled instance of {@link UnixFSTransactionProgram}
     */
    public UnixFSTransactionProgram compile(final UnixFSTransactionProgramExecutionPhase ... executionPhases) {

        if (nodeId == null) throw new IllegalStateException("NodeId must be set.");
        if (byteBuffer == null) throw new IllegalStateException("Byte buffer must be set.");

        final int programPosition = byteBuffer.position();
        for (int i = 0; i < UnixFSTransactionProgramHeader.SIZE; ++i) byteBuffer.put((byte) 0xFF);

        final UnixFSTransactionProgram program = new UnixFSTransactionProgram(byteBuffer, programPosition);
        program.header.algorithm.set(checksumAlgorithm);
        program.header.checksum.set(0);

        int programLength = 0;

        // Determines which phases to commit.

        short phases = 0;

        for (final UnixFSTransactionProgramExecutionPhase executionPhase : executionPhases) {
            phases |= 0x1 << executionPhase.ordinal();
        }

        program.header.phases.set(phases);

        if ((phases & 0x1 << COMMIT.ordinal()) != 0) {
            programLength += compile(COMMIT, program, program.header.commitPos, program.header.commitLen);
        } else {
            program.header.commitPos.set(0);
            program.header.commitLen.set(0);
        }

        if ((phases & 0x1 << CLEANUP.ordinal()) != 0) {
            programLength += compile(CLEANUP, program, program.header.cleanupPos, program.header.cleanupLen);
        } else {
            program.header.cleanupPos.set(0);
            program.header.cleanupLen.set(0);
        }

        program.header.nodeId.set(nodeId);
        program.header.length.set(programLength);
        if (revision != null) program.header.revision.fromRevision(revision);

        return program;

    }

    private int compile(final UnixFSTransactionProgramExecutionPhase executionPhase,
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

    /**
     * Gets the revision to write.
     *
     * @return the node id
     */
    public NodeId getNodeId() {
        return nodeId;
    }

    /**
     * Gets the {@link ByteBuffer} to store the program contents.
     *
     * @return the {@link ByteBuffer}
     */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    /**
     * Gets the {@link UnixFSChecksumAlgorithm} used to commit the program.
     *
     * @return the {@link UnixFSChecksumAlgorithm}
     */
    public UnixFSChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    /**
     * Gets the {@link UnixFSRevision <?>} associated with this builder.
     * @return
     */
    public UnixFSRevision<?> getRevision() {
        return revision;
    }

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
