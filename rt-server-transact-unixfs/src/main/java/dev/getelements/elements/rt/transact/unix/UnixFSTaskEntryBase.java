package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.AbstractTaskEntry;
import dev.getelements.elements.rt.transact.TransactionJournal;

import java.io.IOException;

import static java.lang.String.format;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.*;

public abstract class UnixFSTaskEntryBase extends AbstractTaskEntry<ResourceId> {

    private final UnixFSUtils unixFSUtils;

    public UnixFSTaskEntryBase(
            final OperationalStrategy<ResourceId> operationalStrategy,
            final UnixFSUtils unixFSUtils) {
        super(operationalStrategy);
        this.unixFSUtils = unixFSUtils;
    }

    @Override
    public void flush(final TransactionJournal.MutableEntry mutableEntry) {

        getUnixFSUtils().doOperationV(() -> {
            if (isPresent()) {
                flushContents(mutableEntry);
            } else if (isAbsent()) {
                flushTombstone(mutableEntry);
            } else {
                throw new IllegalStateException("Must be either nascent or present.");
            }
        });

        mutableEntry.applyChangeToTasks(getOriginalScope());

    }

    private void flushContents(final TransactionJournal.MutableEntry mutableEntry) throws IOException {

        final var scope = getScope();
        final var originalScope = getOriginalScope();
        final var transactionId = mutableEntry.getTransactionId();

        if (!originalScope.equals(scope)) {
            throw new IllegalStateException(format(
                    "Original scope must match %s!=%s",
                    getScope(),
                    originalScope
            ));
        }

        final var algo = getUnixFSUtils().getChecksumAlgorithm();
        final var header = new UnixFSDataHeader().setTaskDefaults();
        header.resourceId.set(originalScope);
        header.transactionId.set(mutableEntry.getTransactionId());
        header.checksumAlgorithm.set(algo);

        final var mapping = UnixFSTaskPathMapping.fromResourceId(getUnixFSUtils(), getOriginalScope());

        final var pathForTransaction = mapping
                .createParentDirectories()
                .getFilesystemPath(transactionId);

        try (final var transactionFileChannel = open(pathForTransaction, READ, WRITE, CREATE, TRUNCATE_EXISTING)) {

            UnixFSDataHeader.fill(transactionFileChannel);

            for (final var task : getTasksImmutable().values()) {
                final var unixFSTask = new UnixFSTask();
                unixFSTask.timestamp.set(task.getTimestamp());
                unixFSTask.packedTaskId.set(task.getTaskId());
                unixFSTask.write(transactionFileChannel);
            }

            final var checksum = algo.compute(() -> transactionFileChannel.position(header.size()));
            header.checksum.set(checksum);
            header.writeHeader(algo, transactionFileChannel.position(0));

        }

    }

    private void flushTombstone(final TransactionJournal.MutableEntry mutableEntry) throws IOException {

        final var transactionId = mutableEntry.getTransactionId();
        final var mapping = UnixFSTaskPathMapping.fromResourceId(unixFSUtils, getOriginalScope());
        final var pathForTransaction = mapping
                .createParentDirectories()
                .getFilesystemPath(transactionId);

        getUnixFSUtils().markTombstone(pathForTransaction);

    }

    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

}
