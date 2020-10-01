package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.Files.*;
import static java.util.Collections.reverseOrder;

public class UnixFSGarbageCollection {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSGarbageCollection.class);

    // Injected fields.

    private final UnixFSUtils utils;

    private final UnixFSRevisionPool unixFSRevisionPool;

    private final UnixFSTransactionJournal transactionJournal;

    private final UnixFSTransactionProgramBuilder programBuilder;

    // Variables used within the scope of the collection cycle

    private UnixFSRevision<?> targetRevision;

    private UnixFSRevisionTableEntry targetRevisionTableEntry;

    private final List<Operation> collectOperations = new ArrayList<>();

    private final SortedMap<ResourceId, Operation> resourceIdPreserveOperations = new TreeMap<>(this::sort);

    private final SortedMap<com.namazustudios.socialengine.rt.Path, Operation> pathPreserveOperations = new TreeMap<>(reverseOrder(this::sort));

    @Inject
    public UnixFSGarbageCollection(final UnixFSUtils utils,
                                   final UnixFSRevisionPool unixFSRevisionPool,
                                   final UnixFSTransactionJournal transactionJournal,
                                   final UnixFSTransactionProgramBuilder programBuilder) {
        this.utils = utils;
        this.unixFSRevisionPool = unixFSRevisionPool;
        this.transactionJournal = transactionJournal;
        this.programBuilder = programBuilder;
    }

    private int sort(final com.namazustudios.socialengine.rt.Path l, final com.namazustudios.socialengine.rt.Path r) {
        // We sort paths by their length (in components) and then lexicographically after that. This ensures that the
        // shortest paths are at the beginning of the collection.
        final int lengthDifference = l.getComponents().size() - r.getComponents().size();
        return lengthDifference == 0 ? l.toString().compareTo(r.toString()) : lengthDifference;
    }

    private int sort(final ResourceId l, final ResourceId r) {
        // Less important, but this sorts each ResourceId lexicographically
        return l.toString().compareTo(r.toString());
    }

    public void collectUpTo(final UnixFSRevisionTableEntry targetRevisionTableEntry) {

        collectOperations.clear();
        pathPreserveOperations.clear();
        resourceIdPreserveOperations.clear();

        try {
        } finally {
            this.targetRevision = unixFSRevisionPool.create(targetRevisionTableEntry.revision);
            this.targetRevisionTableEntry = targetRevisionTableEntry;
        }

    }

    private class Link implements ExecutionHandler {

        private final UnixFSRevision<?> revision;

        public Link(final UnixFSTransactionProgram program) {
            this.revision = unixFSRevisionPool.create(program.header.revision);
        }

        @Override
        public void unlinkFile(final UnixFSTransactionProgram program, final Path fsPath) {
            logger.trace("Ignoring FS unlink for link phase {} {}.", program, fsPath);
        }

        @Override
        public void unlinkRTPath(final UnixFSTransactionProgram program,
                                 final com.namazustudios.socialengine.rt.Path rtPath) {
            final NodeId nodeId = program.header.nodeId.get();
            final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);
            final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);
            final Path source = pathMapping.resolveSymlinkPath(revision);
            collectOperations.add(() -> delete(source));
        }

        @Override
        public void removeResource(final UnixFSTransactionProgram program, final ResourceId resourceId) {
            resourceIdPreserveOperations.put(resourceId, () -> {

            });
        }

        @Override
        public void linkResourceToRTPath(final UnixFSTransactionProgram program,
                                         final ResourceId resourceId,
                                         final com.namazustudios.socialengine.rt.Path rtPath) {
            pathPreserveOperations.put(rtPath, () -> {

                final NodeId nodeId = program.header.nodeId.get();
                final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);

                final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);

                final Path source = pathMapping.resolveSymlinkPath(revision);
                final Path destination = pathMapping.resolveSymlinkPath(targetRevision);

                collectOperations.add(() -> delete(source));

                try {
                    createLink(destination, source);
                } catch (FileNotFoundException | FileAlreadyExistsException ex) {
                    logger.info("Error re-linking file {} -> {}", source, destination, ex);
                }

            });
        }

        @Override
        public void updateResource(final UnixFSTransactionProgram program,
                                   final Path fsPath,
                                   final ResourceId resourceId) {

            final UnixFSResourceIdMapping resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

            final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);

            final Path source = resourceIdMapping.resolveRevisionFilePath(revision);
            final Path destination = resourceIdMapping.resolveRevisionFilePath(targetRevision);


            resourceIdPreserveOperations.put(resourceId, () -> {
                try {
                    createLink(destination, source);
                } catch (FileNotFoundException | FileAlreadyExistsException ex) {
                    logger.info("Error re-linking file {} -> {}", source, destination, ex);
                }
            });

        }

        @Override
        public void addPath(final UnixFSTransactionProgram program,
                            final com.namazustudios.socialengine.rt.Path path) {
            logger.trace("Ignoring add path operation {}.", path);
        }

        @Override
        public void addResourceId(final UnixFSTransactionProgram program,
                                  final ResourceId resourceId) {
            logger.trace("Ignoring add resource id operation {}.", resourceId);
        }

        @Override
        public void linkNewResource(final UnixFSTransactionProgram program,
                                    final Path fsPath,
                                    final ResourceId resourceId) {
            resourceIdPreserveOperations.put(resourceId, () -> utils.doOperationV(() -> {

            }, FatalException::new));
        }

    }

    @FunctionalInterface
    private interface Operation { void perform() throws IOException; }

}
