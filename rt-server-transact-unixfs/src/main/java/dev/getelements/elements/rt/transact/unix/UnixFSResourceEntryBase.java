package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.AbstractResourceEntry;
import dev.getelements.elements.rt.transact.FatalException;
import dev.getelements.elements.rt.transact.TransactionJournal;
import dev.getelements.elements.rt.transact.TransactionJournal.MutableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TreeSet;

import static dev.getelements.elements.sdk.cluster.path.Paths.WILDCARD_FIRST;
import static java.lang.String.format;
import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.createLink;
import static java.nio.file.StandardOpenOption.*;

public abstract class UnixFSResourceEntryBase extends AbstractResourceEntry {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSResourceEntryBase.class);

    private final UnixFSUtils unixFSUtils;

    public UnixFSResourceEntryBase(final OperationalStrategy operationalStrategy, final UnixFSUtils unixFSUtils) {
        super(operationalStrategy);
        this.unixFSUtils = unixFSUtils;
    }

    @Override
    public void flush(final MutableEntry mutableEntry) {

        getUnixFSUtils().doOperationV(() -> {

            if (isPresent()) {

                final var resourceId = getResourceId();
                final var originalResourceId = getOriginalResourceId();

                if (!resourceId.equals(originalResourceId)) {

                    // We currently don't support moving the resource. If this changes, we essentially have to tombstone
                    // the old resource and then write the new resource. to the new location and update all links to it.

                    throw new IllegalStateException(format(
                            "Original resource id must match %s!=%s",
                            resourceId,
                            originalResourceId
                    ));

                }

                if (!isOriginalContents()) {
                    mutableEntry.applyChangeToResourceContents(getOriginalResourceId());
                }

                if (!isOriginalReversePaths()) {
                    flushReversePaths(resourceId, mutableEntry);
                    mutableEntry.applyChangeToResourceReversePaths(getOriginalResourceId());
                }

            } else if (isAbsent()) {
                flushTombstones(getOriginalResourceId(), mutableEntry);
            } else {
                throw new IllegalStateException("Must be either nascent or present.");
            }

        });

    }

    private void flushReversePaths(
            final ResourceId resourceId,
            final TransactionJournal.MutableEntry mutableEntry) throws IOException {

        final var transactionId = mutableEntry.getTransactionId();
        final var reversePathMapping = UnixFSReversePathMapping.fromResourceId(getUnixFSUtils(), resourceId);

        final var transactionFileName = reversePathMapping
                .createParentDirectories()
                .getFilesystemPath(transactionId);

        try (final var transactionFileChannel = open(transactionFileName, READ, WRITE, CREATE, TRUNCATE_EXISTING)) {

            UnixFSDataHeader.fill(transactionFileChannel);

            final var sizeByteBuffer = allocate(Integer.BYTES);

            for (var path : getReversePathsImmutable()) {

                final var pathByteBuffer = path.toByteBuffer();
                sizeByteBuffer.clear().putInt(pathByteBuffer.remaining()).flip();

                // Writes the size of the path (in bytes).
                while (sizeByteBuffer.hasRemaining()) {
                    if (transactionFileChannel.write(sizeByteBuffer) < 0) {
                        throw new FatalException("Unexpected end of stream: " + transactionFileName);
                    }
                }

                // Writes the contents of the path.
                while (pathByteBuffer.hasRemaining()) {
                    if (transactionFileChannel.write(pathByteBuffer) < 0) {
                        throw new FatalException("Unexpected end of stream: " + transactionFileName);
                    }
                }

            }

            final var algo = getUnixFSUtils().getChecksumAlgorithm();
            final var header = new UnixFSDataHeader().setReversePathDefaults();
            final var checksum = algo.compute(() -> transactionFileChannel.position(header.size()));

            header.checksum.set(checksum);
            header.resourceId.set(resourceId);
            header.transactionId.set(transactionId);
            header.writeHeader(algo, transactionFileChannel.position(0));

        }

        final var existing = reversePathMapping.getFilesystemPath(transactionId);

        for (var path : getReversePathsImmutable()) {

            final var link = UnixFSPathMapping
                    .fromRTPath(getUnixFSUtils(), path)
                    .createParentDirectories()
                    .getFilesystemPath(transactionId);

            getUnixFSUtils().doOperation(() -> createLink(link, existing));

        }

        final var toRemove = new TreeSet<>(WILDCARD_FIRST);
        toRemove.addAll(getOriginalReversePathsImmutable());
        toRemove.removeAll(getReversePathsImmutable());

        for (var path : toRemove) {
            final var pathMapping = UnixFSPathMapping.fromRTPath(getUnixFSUtils(), path);
            getUnixFSUtils().markTombstone(pathMapping.getFilesystemPath(transactionId));
        }

        final var affectedPaths = new TreeSet<>(WILDCARD_FIRST);
        affectedPaths.addAll(getReversePathsImmutable());
        affectedPaths.addAll(getOriginalReversePathsImmutable());
        affectedPaths.forEach(mutableEntry::applyChangeToResourceReversePaths);

    }

    private void flushTombstones(
            final ResourceId resourceId,
            final TransactionJournal.MutableEntry mutableEntry) {

        final var transactionId = mutableEntry.getTransactionId();
        final var contentsMapping = UnixFSResourceContentsMapping.fromResourceId(getUnixFSUtils(), resourceId);
        final var reversePathsMapping = UnixFSReversePathMapping.fromResourceId(getUnixFSUtils(), resourceId);

        if (getUnixFSUtils().isRegularFile(contentsMapping.getFilesystemPath())) {
            mutableEntry.applyChangeToResourceContents(getOriginalResourceId());
            getUnixFSUtils().markTombstone(contentsMapping.getFilesystemPath(transactionId));
        }

        mutableEntry.applyChangeToResourceReversePaths(getOriginalResourceId());
        getUnixFSUtils().markTombstone(reversePathsMapping.getFilesystemPath(transactionId));

        for (var path : getOriginalReversePathsImmutable()) {
            final var pathMapping = UnixFSPathMapping.fromRTPath(getUnixFSUtils(), path);
            mutableEntry.applyChangeToResourceReversePaths(path);
            getUnixFSUtils().markTombstone(pathMapping.getFilesystemPath(transactionId));
        }

    }

    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

}
