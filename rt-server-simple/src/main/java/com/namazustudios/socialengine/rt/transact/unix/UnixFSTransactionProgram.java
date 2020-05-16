package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionCommand.Instruction.*;
import static java.util.Objects.requireNonNull;

public class UnixFSTransactionProgram {

    private final Header header = new Header();

    public UnixFSTransactionProgram(final ByteBuffer byteBuffer, final int programPosition) {
        header.setByteBuffer(byteBuffer, programPosition);
    }

    public void commit() {
        // TODO Implement This.
    }

    public static class Builder {

        private ByteBuffer byteBuffer;

        private ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.ADLER_32;

        private final List<CommandWriter> operations = new ArrayList<>();

        public Builder withByteBuffer(final ByteBuffer byteBuffer) {
            requireNonNull(byteBuffer);
            this.byteBuffer = byteBuffer;
            return this;
        }

        public Builder withChecksumAlgorithm(final ChecksumAlgorithm checksumAlgorithm) {
            requireNonNull(checksumAlgorithm);
            this.checksumAlgorithm = checksumAlgorithm;
            return this;
        }

        public Builder unlinkFile(final UnixFSTransactionCommand.Phase phase,
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

        public Builder unlinkResource(final UnixFSTransactionCommand.Phase phase,
                                      final com.namazustudios.socialengine.rt.Path rtPath) {

            requireNonNull(phase);
            requireNonNull(rtPath);

            operations.add((byteBuffer -> UnixFSTransactionCommand.builder()
                    .withPhase(phase)
                    .withInstruction(UNLINK_RESOURCE)
                    .addRTPathParameter(rtPath)
                .build(byteBuffer)));

            return this;

        }

        public Builder linkFile(final UnixFSTransactionCommand.Phase phase,
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

        public Builder linkFile(final UnixFSTransactionCommand.Phase phase,
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

        public Builder linkResource(final UnixFSTransactionCommand.Phase phase,
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

        public UnixFSTransactionProgram compile() {

            if (byteBuffer == null) throw new IllegalStateException("Byte buffer must be set.");

            final int programPosition = byteBuffer.position();
            for (int i = 0; i < UnixFSTransactionCommand.Header.SIZE; ++i) byteBuffer.put((byte)0xFF);

            final UnixFSTransactionProgram program = new UnixFSTransactionProgram(byteBuffer, programPosition);
            program.header.algorithm.set(checksumAlgorithm);
            program.header.checksum.set(0);
            operations.forEach(writer -> writer.write(byteBuffer));

            return program;

        }

        public Builder removeResource(final UnixFSTransactionCommand.Phase phase, final ResourceId resourceId) {
            return this;
        }

        public Builder deletePath(final UnixFSTransactionCommand.Phase phase, final Path path) {
            return this;
        }

    }

    public static class Header extends Struct {

        public static final int SIZE = new Header().size();

        private final Enum8<ChecksumAlgorithm> algorithm = new Enum8<>(ChecksumAlgorithm.values());

        private final Unsigned32 checksum = new Unsigned32();

        private final Unsigned32 length = new Unsigned32();

    }

    /**
     * Which checksum algorithm to use when committing the transaction.
     */
    public enum ChecksumAlgorithm {

        /**
         * Uses {@link CRC32}
         */
        CRC_32,

        /**
         * Uses {@link Adler32}
         */
        ADLER_32

    }

    @FunctionalInterface
    public interface CommandWriter {

        void write(ByteBuffer buffer);

    }

}
