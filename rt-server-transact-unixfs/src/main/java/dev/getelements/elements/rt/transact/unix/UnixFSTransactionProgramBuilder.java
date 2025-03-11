package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import javolution.io.Struct;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionCommandInstruction.*;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.CLEANUP;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.COMMIT;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Used to compile a set of instructions into a {@link UnixFSTransactionProgram} used for committing the requested
 * transactions to disk. This does no checking of the underlying database, and assumes that the calling code has
 * properly completed all pre-checks while building the code.
 */
public class UnixFSTransactionProgramBuilder {

    private NodeId nodeId;

    private String transactionId;

    private ByteBuffer byteBuffer;

    private UnixFSChecksumAlgorithm checksumAlgorithm = UnixFSChecksumAlgorithm.ADLER_32;

    private final Map<UnixFSTransactionProgramExecutionPhase, List<CommandWriter>> operations = new EnumMap<>(UnixFSTransactionProgramExecutionPhase.class);

    private final Map<UnixFSTransactionProgramExecutionPhase, List<UnixFSTransactionCommand>> commands = new EnumMap<>(UnixFSTransactionProgramExecutionPhase.class);

    /**
     * Specifies the {@link NodeId} to associate with the program. The {@link NodeId} is used to
     *
     * @param nodeId the {@link NodeId}
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder withNodeId(final NodeId nodeId) {
        requireNonNull(nodeId);
        this.nodeId = nodeId;
        return this;
    }

    /**
     * Gets the id for the currently running transaction.
     *
     * @param transactionId the transaction id.
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder withTransactionId(final String transactionId) {
        requireNonNull(transactionId);
        this.transactionId = transactionId;
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
        return this;
    }

    /**
     * Applies the changes for the supplied {@link Path} to the data store.
     *
     * @param executionPhase  the execution phase to use
     * @param path the {@link Path} to apply
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder applyReversePathChangeToResource(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final Path path) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(path, "path");

        if (transactionId == null) {
            throw new IllegalStateException("No transaction ID currently assigned.");
        }

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(APPLY_PATH_CHANGE_FOR_RESOURCE)
                .addStringParameter(transactionId)
                .addRTPathParameter(path)
                .build(byteBuffer)));

        return this;
    }

    /**
     * Applies the contents change ot the resource.
     *
     * @param executionPhase  the execution phase to use
     * @param resourceId the {@link ResourceId} to apply
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder applyChangeToResourceContents(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final ResourceId resourceId) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(resourceId, "resourceId");

        if (transactionId == null) {
            throw new IllegalStateException("No transaction ID currently assigned.");
        }

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(APPLY_CONTENTS_CHANGE_FOR_RESOURCE)
                .addStringParameter(transactionId)
                .addResourceIdParameter(resourceId)
                .build(byteBuffer)));

        return this;

    }

    /**
     * Applies the supplied {@link ResourceId} changes to the data store.
     *
     * @param executionPhase  the execution phase to use
     * @param resourceId the {@link ResourceId} to apply
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder applyReversePathChangeToResource(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final ResourceId resourceId) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(resourceId, "resourceId");

        if (transactionId == null) {
            throw new IllegalStateException("No transaction ID currently assigned.");
        }

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(APPLY_REVERSE_PATH_CHANGE_FOR_RESOURCE)
                .addStringParameter(transactionId)
                .addResourceIdParameter(resourceId)
                .build(byteBuffer)));

        return this;
    }

    /**
     * Applies the supplied {@link ResourceId} changes to the data store for the associated tasks.
     *
     * @param executionPhase  the execution phase to use
     * @param resourceId the {@link Path} to apply
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder applyChangeToTasks(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final ResourceId resourceId) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(resourceId, "resourceId");

        if (transactionId == null) {
            throw new IllegalStateException("No transaction ID currently assigned.");
        }

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(APPLY_TASK_CHANGES_FOR_RESOURCE_ID)
                .addStringParameter(transactionId)
                .addResourceIdParameter(resourceId)
                .build(byteBuffer)));

        return this;

    }

    /**
     * Cleans up the supplied {@link Path} changes from the data store.
     *
     * @param executionPhase  the execution phase to use
     * @param path the {@link Path} to apply
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder cleanupResource(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final Path path) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(path, "path");

        if (transactionId == null) {
            throw new IllegalStateException("No transaction ID currently assigned.");
        }

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(CLEANUP_RESOURCE_AT_PATH)
                .addStringParameter(transactionId)
                .addRTPathParameter(path)
                .build(byteBuffer)));

        return this;

    }

    /**
     * Cleans up the supplied {@link ResourceId} changes from the data store.
     *
     * @param executionPhase  the execution phase to use
     * @param resourceId the {@link Path} to apply
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder cleanupResource(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final ResourceId resourceId) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(resourceId, "resourceId");

        if (transactionId == null) {
            throw new IllegalStateException("No transaction ID currently assigned.");
        }

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(CLEANUP_RESOURCE_FOR_RESOURCE_ID)
                .addStringParameter(transactionId)
                .addResourceIdParameter(resourceId)
                .build(byteBuffer)));

        return this;

    }

    /**
     * Cleans up the tasks for the supplied {@link ResourceId}.
     *
     * @param executionPhase the execution phase
     * @param resourceId the {@link ResourceId}
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder cleanupTasksForResource(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final ResourceId resourceId) {

        requireNonNull(executionPhase, "executionPhase");
        requireNonNull(resourceId, "resourceId");

        if (transactionId == null) {
            throw new IllegalStateException("No transaction ID currently assigned.");
        }

        getOperations(executionPhase).add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(executionPhase)
                .withInstruction(CLEANUP_TASKS_FOR_RESOURCE_ID)
                .addStringParameter(transactionId)
                .addResourceIdParameter(resourceId)
                .build(byteBuffer)));

        return this;

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

        this.commands.clear();

        if (getNodeId() == null) throw new IllegalStateException("NodeId must be set.");
        if (getByteBuffer() == null) throw new IllegalStateException("Byte buffer must be set.");

        final int programPosition = getByteBuffer().position();
        for (int i = 0; i < UnixFSTransactionProgramHeader.SIZE; ++i) getByteBuffer().put((byte) 0xFF);

        final UnixFSTransactionProgram program = new UnixFSTransactionProgram(getByteBuffer(), programPosition);
        program.header.setDefaults();
        program.header.transactionId.set(getTransactionId());
        program.header.algorithm.set(getChecksumAlgorithm());
        program.header.checksum.set(0);

        int programLength = 0;

        // Determines which phases to commit.

        short phases = 0;

        for (final UnixFSTransactionProgramExecutionPhase executionPhase : executionPhases) {
            phases |= 0x1 << executionPhase.ordinal();
        }

        program.header.phases.set(phases);

        if (COMMIT.isEnabled(phases)) {
            programLength += compile(COMMIT, program, program.header.commitPos, program.header.commitLen);
        } else {
            program.header.commitPos.set(0);
            program.header.commitLen.set(0);
        }

        if (CLEANUP.isEnabled(phases)) {
            programLength += compile(CLEANUP, program, program.header.cleanupPos, program.header.cleanupLen);
        } else {
            program.header.cleanupPos.set(0);
            program.header.cleanupLen.set(0);
        }

        program.header.length.set(programLength);

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
     * Gets the transaction ID.
     * @return the transaction id
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Gets the {@link UnixFSChecksumAlgorithm} used to commit the program.
     *
     * @return the {@link UnixFSChecksumAlgorithm}
     */
    public UnixFSChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
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
