package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.ResourceId;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionCommand.Instruction.*;
import static java.util.Objects.requireNonNull;

/**
 * Used to compile a set of instructions into a {@link UnixFSTransactionProgram} used for committing the requested
 * transactions to disk. This does no checking of the underlying database, and assumes that the calling code has
 * properly completed all pre-checks while building the code.
 */
public class UnixFSTransactionProgramBuilder {

    private ByteBuffer byteBuffer;

    private UnixFSChecksumAlgorithm checksumAlgorithm = UnixFSChecksumAlgorithm.ADLER_32;

    private final List<UnixFSTransactionProgram.CommandWriter> operations = new ArrayList<>();

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
     * Adds a step to the program to unlink a {@link java.nio.file.Path}. Must point to a file.
     *
     * @param phase  the execution phase to use
     * @param fsPath the {@link java.nio.file.Path} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder unlinkFile(final UnixFSTransactionCommand.Phase phase,
                                                      final java.nio.file.Path fsPath) {

        requireNonNull(phase);
        requireNonNull(fsPath);

        operations.add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(phase)
                .withInstruction(UNLINK_FS_PATH)
                .addFSPathParameter(fsPath)
            .build(byteBuffer)));

        return this;

    }

    /**
     * Adds a step to the program to unlink a {@link com.namazustudios.socialengine.rt.Path} from the revision store.
     *
     * @param phase  the execution phase to use
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder unlinkResource(final UnixFSTransactionCommand.Phase phase,
                                                          final com.namazustudios.socialengine.rt.Path rtPath) {

        requireNonNull(phase);
        requireNonNull(rtPath);

        operations.add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(phase)
                .withInstruction(UNLINK_RESOURCE_ID_FROM_PATH)
                .addRTPathParameter(rtPath)
            .build(byteBuffer)));

        return this;

    }

    /**
     * Unlinks the {@link java.nio.file.Path} pointing to a specific {@link ResourceId}.
     *
     * @param phase  the execution phase to use
     * @param fsPath the {@link java.nio.file.Path} to unlink
     * @param resourceId the {@link ResourceId} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder linkFile(final UnixFSTransactionCommand.Phase phase,
                                                    final java.nio.file.Path fsPath,
                                                    final ResourceId resourceId) {

        requireNonNull(phase);
        requireNonNull(fsPath);
        requireNonNull(resourceId);

        operations.add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(phase)
                .withInstruction(LINK_FS_PATH_TO_RESOURCE_ID)
                .addFSPathParameter(fsPath)
                .addResourceIdParameter(resourceId)
            .build(byteBuffer)));

        return this;
    }

    /**
     * Unlinks the {@link java.nio.file.Path} pointing to a specific {@link com.namazustudios.socialengine.rt.Path}.
     *
     * @param phase  the execution phase to use
     * @param fsPath the {@link java.nio.file.Path} to unlink
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path} to unlink
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder linkFile(final UnixFSTransactionCommand.Phase phase,
                                                    final java.nio.file.Path fsPath,
                                                    final com.namazustudios.socialengine.rt.Path rtPath) {

        requireNonNull(phase);
        requireNonNull(fsPath);
        requireNonNull(rtPath);

        operations.add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(phase)
                .withInstruction(LINK_FS_PATH_TO_RT_PATH)
                .addFSPathParameter(fsPath)
                .addRTPathParameter(rtPath)
            .build(byteBuffer)));

        return this;

    }

    /**
     * Links a {@link ResourceId} to a specific {@link com.namazustudios.socialengine.rt.Path}.
     *
     * @param phase  the execution phase to use
     * @param resourceId the {@link ResourceId} to link
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path} to link
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder linkResource(final UnixFSTransactionCommand.Phase phase,
                                                        final ResourceId resourceId,
                                                        final com.namazustudios.socialengine.rt.Path rtPath) {

        requireNonNull(phase);
        requireNonNull(resourceId);
        requireNonNull(rtPath);

        operations.add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(phase)
                .withInstruction(LINK_RESOURCE_TO_RT_PATH)
                .addResourceIdParameter(resourceId)
                .addRTPathParameter(rtPath)
            .build(byteBuffer)));

        return this;

    }

    /**
     * Removes a {@link ResourceId} from the database.
     *
     * @param phase  the execution phase to use
     * @param resourceId the {@link ResourceId} to remove
     *
     * @return this instance
     */
    public UnixFSTransactionProgramBuilder removeResource(final UnixFSTransactionCommand.Phase phase,
                                                          final ResourceId resourceId) {

        requireNonNull(phase);
        requireNonNull(resourceId);

        operations.add((byteBuffer -> UnixFSTransactionCommand.builder()
                .withPhase(phase)
                .withInstruction(REMOVE_RESOURCE)
                .addResourceIdParameter(resourceId)
            .build(byteBuffer)));

        return this;

    }

    /**
     * Compiles this program, setting the header values as well as writing the bytecode to {@link ByteBuffer} containing
     * the program's instructions. This does clear the checksum header, however, it does does not calculate the
     * final checksum as that is part of the committing process.
     *
     * @return a freshly compiled instance of {@link UnixFSTransactionProgram}
     */
    public UnixFSTransactionProgram compile() {

        if (byteBuffer == null) throw new IllegalStateException("Byte buffer must be set.");

        final int programPosition = byteBuffer.position();
        for (int i = 0; i < UnixFSTransactionProgram.Header.SIZE; ++i) byteBuffer.put((byte)0xFF);

        final UnixFSTransactionProgram program = new UnixFSTransactionProgram(byteBuffer, programPosition);
        program.header.algorithm.set(checksumAlgorithm);
        program.header.checksum.set(0);
        operations.forEach(writer -> writer.write(byteBuffer));

        return program;

    }

}
