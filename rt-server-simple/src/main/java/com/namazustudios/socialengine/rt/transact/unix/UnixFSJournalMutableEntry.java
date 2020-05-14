package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionCommand.Phase.CLEANUP;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionCommand.Phase.COMMIT;
import static java.nio.file.StandardOpenOption.WRITE;

class UnixFSJournalMutableEntry extends UnixFSJournalEntry implements TransactionJournal.MutableEntry {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSJournalMutableEntry.class);

    // Initialized with Object

    private boolean committed = false;

    private UnixFSWorkingCopy workingCopy;

    // Assigned in Constructor

    private final UnixFSUtils unixFSUtils;

    private final UnixFSPathIndex unixFSPathIndex;

    private final UnixFSTransactionProgram.Builder programBuilder;

    private final UnixFSOptimisticLocking optimisticLocking;

    public UnixFSJournalMutableEntry(final Revision<?> revision,
                                     final UnixFSUtils unixFSUtils,
                                     final UnixFSPathIndex unixFSPathIndex,
                                     final UnixFSTransactionProgram.Builder programBuilder,
                                     final UnixFSUtils.IOOperationV onClose,
                                     final UnixFSOptimisticLocking optimisticLocking) {
        super(revision, onClose);
        this.programBuilder = programBuilder;
        this.unixFSUtils = unixFSUtils;
        this.unixFSPathIndex = unixFSPathIndex;
        this.optimisticLocking = optimisticLocking;
        this.workingCopy = new UnixFSWorkingCopy(revision, unixFSPathIndex, optimisticLocking);
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
                    programBuilder.linkFile(COMMIT, temporaryFile, resourceId)
                            .linkFile(COMMIT, temporaryFile, path)
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

        // Gets the ResourceId from the working copy. Throwing if it does not exist for this revision.
        if (!workingCopy.isPresent(path)) throw new ResourceNotFoundException("No resource at " + path);
        optimisticLocking.lock(path);
        return workingCopy.unlink(path, () -> programBuilder.unlinkResource(COMMIT, path));

    }

    @Override
    public List<ResourceService.Unlink> unlinkMultiple(
            final Path path,
            final int max) throws TransactionConflictException {

        check();

        return workingCopy.unlinkMultiple(path, max, (fqPath, resourceId, remove) -> {
            programBuilder.unlinkResource(COMMIT, fqPath);
            if (remove) programBuilder.remove(COMMIT, resourceId);
        });

//        final List<ResourceId> toPurge = new ArrayList<>();
//        final Map<ResourceId, Set<Path>> allPaths = new HashMap<>();
//
//        unixFSPathIndex
//            .list(revision, path)
//            .getValue()
//            .ifPresent(ls -> ls.forEach(l -> {
//
//                final Set<Path> paths = unixFSPathIndex
//                    .getReverseRevisionMap()
//                    .getValueAt(revision, l.getResourceId())
//                    .getValue().orElseThrow(() -> new FatalException("Reverse path mismatch."));
//
//                allPaths
//                    .computeIfAbsent(l.getResourceId(), rid -> new TreeSet<>())
//                    .addAll(paths);
//
//            }));

//        for (final Map.Entry<ResourceId, Set<Path>> entry : allPaths.entrySet()) {
//            optimisticLocking.lock(entry.getKey());
//            optimisticLocking.lockPaths(entry.getValue());
//        }
//
//        return toPurge
//            .stream()
//            .map(resourceId -> {
//
//                allPaths.get(resourceId).remove(resourceId);
//
//                final boolean removed = allPaths.get(resourceId).isEmpty();
//
//                programBuilder.unlinkResource(COMMIT, path);
//                programBuilder.remove(COMMIT, resourceId);
//
//
//
//                return new ResourceService.Unlink() {
//
//
//                    @Override
//                    public ResourceId getResourceId() {
//                        return resourceId;
//                    }
//
//                    @Override
//                    public boolean isRemoved() {
//                        return removed;
//                    }
//
//                };
//
//            }).collect(toList());

    }

    @Override
    public void removeResource(final ResourceId resourceId) throws TransactionConflictException {
        check();
        workingCopy.removeResource(resourceId, () -> programBuilder.remove(COMMIT, resourceId));
    }

    @Override
    public List<ResourceId> removeResources(final Path path, final int max) throws TransactionConflictException {
        check();

        return workingCopy.removeResources(path, max,
            (fqPath, resourceId) -> programBuilder.remove(COMMIT, resourceId)
                                                  .deletePath(COMMIT, fqPath));

    }

    @Override
    public void commit() {
        check();
        programBuilder.compile().commit();
        committed = true;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    protected void check() {
        super.check();
        if (committed) throw new IllegalStateException();
    }

}
