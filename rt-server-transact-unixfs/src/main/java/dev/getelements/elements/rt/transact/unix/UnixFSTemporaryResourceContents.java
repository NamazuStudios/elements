package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.transact.ResourceContents;
import javolution.io.Struct;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.*;

public class UnixFSTemporaryResourceContents implements ResourceContents {

    private final UnixFSResourceContentsMapping mapping;

    public UnixFSTemporaryResourceContents(final UnixFSResourceContentsMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public ReadableByteChannel read() throws IOException {
        return open(mapping.getFilesystemPath(), READ).position(UnixFSDataHeader.SIZE);
    }

    @Override
    public Optional<WritableByteChannel> tryWrite(final String transactionId) throws IOException {

        final var algo = mapping.getUtils().getChecksumAlgorithm();
        final var header = new UnixFSDataHeader().setResourceContentsDefaults();

        header.checksumAlgorithm.set(algo);
        header.transactionId.set(transactionId);
        header.resourceId.set(mapping.getResourceId());
        algo.compute(header);

        final var fsPath = mapping
                .createParentDirectories()
                .getFilesystemPath(transactionId);

        final var fileChannel = open(fsPath, READ, WRITE, CREATE, TRUNCATE_EXISTING);
        UnixFSDataHeader.fill(fileChannel);

        return Optional.of(new WritableByteChannel() {

            @Override
            public boolean isOpen() {
                return fileChannel.isOpen();
            }

            @Override
            public int write(final ByteBuffer src) throws IOException {
                return fileChannel.write(src);
            }

            @Override
            public void close() throws IOException {
                try {
                    if (fileChannel.isOpen()) {
                        final var checksum = algo.compute(() -> fileChannel.position(header.size()));
                        header.checksum.set(checksum);
                        header.writeHeader(algo, fileChannel.position(0));
                    }
                } finally {
                    fileChannel.close();
                }
            }

        });

    }

}
