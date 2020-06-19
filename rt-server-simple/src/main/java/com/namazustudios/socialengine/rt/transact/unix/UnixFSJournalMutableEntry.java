package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase.CLEANUP;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase.COMMIT;
import static java.nio.file.StandardOpenOption.WRITE;

class UnixFSJournalMutableEntry extends UnixFSJournalEntry implements TransactionJournal.MutableEntry {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSJournalMutableEntry.class);

    // Initialized with Object

    private boolean rollback = false;

    private boolean committed = false;

    private UnixFSWorkingCopy workingCopy;

    private UnixFSTransactionProgram program = null;

    // Assigned in Constructor

    private final UnixFSUtils unixFSUtils;

    private final UnixFSTransactionProgramBuilder programBuilder;

    private final UnixFSOptimisticLocking optimisticLocking;

    public UnixFSJournalMutableEntry(final NodeId nodeId,
                                     final Revision<?> revision,
                                     final UnixFSUtils unixFSUtils,
                                     final UnixFSPathIndex unixFSPathIndex,
                                     final UnixFSTransactionProgramBuilder programBuilder,
                                     final UnixFSUtils.IOOperationV onClose,
                                     final UnixFSOptimisticLocking optimisticLocking) {
        super(nodeId, revision, onClose);
        this.programBuilder = programBuilder;
        this.unixFSUtils = unixFSUtils;
        this.optimisticLocking = optimisticLocking;
        this.workingCopy = new UnixFSWorkingCopy(nodeId, revision, unixFSPathIndex, optimisticLocking);
    }

    @Override
    public WritableByteChannel saveNewResource(
            final Path path,
            final ResourceId resourceId) throws IOException, TransactionConflictException {

        check();

        return workingCopy.saveNewResource(path, resourceId, () -> {

            final java.nio.file.Path temporaryFile = unixFSUtils.allocateTemporaryFile();
            final FileChannel fileChannel = FileChannel.open(temporaryFile, WRITE);

            return new WritableByteChannel() {

                @Override
                public int write(ByteBuffer byteBuffer) throws IOException {
                    return fileChannel.write(byteBuffer);
                }

                @Override
                public boolean isOpen() {
                    return fileChannel.isOpen();
                }

                @Override
                public void close() throws IOException {
                    fileChannel.close();
                    programBuilder
                        .linkResourceFile(COMMIT, temporaryFile, path)
                        .linkResourceFile(COMMIT, temporaryFile, resourceId)
                        .unlinkFile(CLEANUP, temporaryFile);
                }

            };

        });

    }

    @Override
    public void linkNewResource(
            final ResourceId sourceResourceId,
            final Path destination) throws TransactionConflictException {
        check();
        workingCopy.linkNewResource(
            sourceResourceId,
            destination,
            () -> programBuilder.linkResource(COMMIT, sourceResourceId, destination));
    }

    @Override
    public void linkExistingResource(
            final ResourceId sourceResourceId,
            final Path destination) throws TransactionConflictException {
        check();
        workingCopy.linkExistingResource(
            sourceResourceId,
            destination,
            () -> programBuilder.linkResource(COMMIT, sourceResourceId, destination));
    }

    @Override
    public ResourceService.Unlink unlinkPath(final Path path) throws TransactionConflictException {
        check();
        if (path.isWildcard()) throw new IllegalArgumentException("Wildcard paths not supported.");
        return workingCopy.unlink(path, () -> programBuilder.unlinkResource(COMMIT, path));
    }

    @Override
    public List<ResourceService.Unlink> unlinkMultiple(
            final Path path,
            final int max) throws TransactionConflictException {

        check();

        return workingCopy.unlinkMultiple(path, max, (fqPath, resourceId, remove) -> {
            programBuilder.unlinkResource(COMMIT, fqPath);
            if (remove) programBuilder.removeResource(COMMIT, resourceId);
        });

    }

    @Override
    public void removeResource(final ResourceId resourceId) throws TransactionConflictException {
        check();
        workingCopy.removeResource(resourceId, path -> programBuilder.unlinkResource(COMMIT, path));
        programBuilder.removeResource(COMMIT, resourceId);
    }

    @Override
    public List<ResourceId> removeResources(final Path path, final int max) throws TransactionConflictException {

        check();

        final List<ResourceId> removed = workingCopy.removeResources(
            path, max,
            (fqPath, resourceId) -> programBuilder.unlinkResource(COMMIT, fqPath)
        );

        removed.forEach(resourceId -> programBuilder.removeResource(COMMIT, resourceId));
        return removed;

    }

    @Override
    public void commit() {
        check();
        program = programBuilder.compile().commit(ExecutionPhase.values());
        committed = true;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    public void rollback() {
        check();
        program = programBuilder.compile().commit(CLEANUP);
        rollback = true;
    }

    public void cleanup(final ExecutionHandler handler) {
        program.interpreter().executeCleanupPhase(handler);
    }

    @Override
    protected void check() {
        super.check();
        if (committed || rollback) throw new IllegalStateException();
    }

    public void apply(final ExecutionHandler handler) {
        if (program == null) throw new IllegalStateException();
        program.interpreter().executeCommitPhase(handler);
    }

}
