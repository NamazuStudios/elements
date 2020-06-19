package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.PathIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.id.ResourceId.getSizeInBytes;
import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromByteBuffer;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.stream.Collectors.toSet;

public class UnixFSPathIndex implements PathIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSPathIndex.class);

    private final NodeId nodeId;

    private final UnixFSUtils utils;

    private final UnixFSGarbageCollector garbageCollector;

    @Inject
    public UnixFSPathIndex(
            final NodeId nodeId,
            final UnixFSUtils utils,
            final UnixFSGarbageCollector garbageCollector) throws IOException {
        this.utils = utils;
        this.nodeId = nodeId;
        this.garbageCollector = garbageCollector;
        createDirectories(utils.getPathStorageRoot());
    }

    @Override
    public RevisionMap<com.namazustudios.socialengine.rt.Path, ResourceId> getRevisionMap(
            final NodeId nodeId) {
        return new PathRevisionMap(nodeId);
    }

    @Override
    public RevisionMap<ResourceId, Set<com.namazustudios.socialengine.rt.Path>> getReverseRevisionMap(
            final NodeId nodeId) {
        return new ReversePathRevisionMap(nodeId);
    }

    @Override
    public Revision<Stream<ResourceService.Listing>> list(final NodeId nodeId,
                                                          final Revision<?> revision,
                                                          final com.namazustudios.socialengine.rt.Path rtPath) {

        final UnixFSPathMapping mapping = UnixFSPathMapping.fromPath(utils, nodeId, rtPath);

        return utils.doOperation(() -> {

            final Stream<ResourceService.Listing> listings = Files
                .walk(mapping.getPathDirectory())
                .filter(Files::isDirectory)
                .map(directory -> loadRevisionListing(mapping, revision))
                .filter(optional -> optional.isPresent())
                .map(optional -> optional.get());

            return revision.withValue(listings);

        });

    }

    public Optional<RevisionListing> loadRevisionListing(final UnixFSPathMapping mapping, final Revision<?> revision) {

        final Path dir = mapping.getPathDirectory();

        return utils.findLatestForRevision(dir, revision).getValue().map(file -> utils.doOperation(() ->{

            final Path pinned = garbageCollector.pin(file, revision);

            try (final FileChannel fc = FileChannel.open(pinned, READ)) {

                final UnixFSObjectHeader objectHeader = new UnixFSObjectHeader();
                final ByteBuffer headerBuffer = ByteBuffer.allocate(objectHeader.size());
                fc.read(headerBuffer);
                headerBuffer.rewind();
                objectHeader.setByteBuffer(headerBuffer, 0);

                final ByteBuffer resourceIdBuffer = ByteBuffer.allocate(getSizeInBytes());
                fc.read(resourceIdBuffer);
                resourceIdBuffer.rewind();

                final ResourceId resourceId = resourceIdFromByteBuffer(resourceIdBuffer);
                return new RevisionListing(mapping, resourceId, objectHeader);

            }

        }));
    }

    public void unlink(final Revision<?> revision,
                       final com.namazustudios.socialengine.rt.Path rtPath) {
        // TODO Figure out unlinking
    }

    public void linkFSPathToRTPath(final Revision<?> revision,
                                   final com.namazustudios.socialengine.rt.Path rtPath,
                                   final java.nio.file.Path sourceFilePath) {

        final UnixFSPathMapping mapping = UnixFSPathMapping.fromPath(utils, nodeId, rtPath);

        utils.doOperationV(() -> {
            final Path revisionPath = mapping.resolveRevisionFilePath(revision);
            createLink(sourceFilePath, revisionPath);
        }, FatalException::new);

    }

    private class RevisionListing implements ResourceService.Listing {

        private final UnixFSPathMapping mapping;

        private final ResourceId resourceId;

        private final UnixFSObjectHeader objectHeader;

        private RevisionListing(final UnixFSPathMapping mapping,
                                final ResourceId resourceId,
                                final UnixFSObjectHeader objectHeader) {
            this.mapping = mapping;
            this.resourceId = resourceId;
            this.objectHeader = objectHeader;
        }

        @Override
        public com.namazustudios.socialengine.rt.Path getPath() {
            return mapping.getPath();
        }

        @Override
        public ResourceId getResourceId() {
            return resourceId;
        }

    }

    private class PathRevisionMap implements RevisionMap<com.namazustudios.socialengine.rt.Path, ResourceId> {

        private final NodeId nodeId;

        public PathRevisionMap(final NodeId nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public Revision<ResourceId> getValueAt(final Revision<?> revision,
                                               final com.namazustudios.socialengine.rt.Path key) {
            final UnixFSPathMapping mapping = UnixFSPathMapping.fromPath(utils, nodeId, key);
            final Optional<RevisionListing> optionalRevisionListing = loadRevisionListing(mapping, revision);
            return revision.withOptionalValue(optionalRevisionListing).map(l -> l.resourceId);
        }

    }

    private class ReversePathRevisionMap implements RevisionMap<ResourceId, Set<com.namazustudios.socialengine.rt.Path>> {

        private final NodeId nodeId;

        public ReversePathRevisionMap(NodeId nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public Revision<Set<com.namazustudios.socialengine.rt.Path>> getValueAt(final Revision<?> revision,
                                                                                final ResourceId key) {

            final UnixFSResourceIdMapping resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils, key);
            final Path reverseDirectory = resourceIdMapping.resolveReverseDirectory(nodeId, revision);

            final Set<com.namazustudios.socialengine.rt.Path> pathSet =
                utils.doOperation(() -> Files.list(reverseDirectory)
                    .filter(path -> isSymbolicLink(path))
                    .map(symlink -> UnixFSPathMapping.fromSymlinkPath(utils, nodeId, symlink))
                    .map(mapping -> mapping.getPath())
                    .collect(toSet()) , FatalException::new);

            return revision.withValue(pathSet);

        }

    }

}
