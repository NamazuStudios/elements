package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.PathIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionFactory;
import com.namazustudios.socialengine.rt.transact.RevisionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.id.ResourceId.getSizeInBytes;
import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromByteBuffer;
import static java.nio.file.StandardOpenOption.READ;

public class UnixFSPathIndex implements PathIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSPathIndex.class);

    private final NodeId nodeId;

    private final UnixFSUtils utils;

    private final RevisionFactory revisionFactory;

    private final UnixFSGarbageCollector garbageCollector;

    private final PathRevisionMap pathRevisionMap;

    @Inject
    public UnixFSPathIndex(
            final NodeId nodeId,
            final UnixFSUtils utils,
            final RevisionFactory revisionFactory,
            final UnixFSGarbageCollector garbageCollector) {
        this.nodeId = nodeId;
        this.utils = utils;
        this.revisionFactory = revisionFactory;
        this.garbageCollector = garbageCollector;
        this.pathRevisionMap = new PathRevisionMap();
    }

    @Override
    public RevisionMap<com.namazustudios.socialengine.rt.Path, ResourceId> getRevisionMap() {
        return pathRevisionMap;
    }

    @Override
    public Revision<Stream<ResourceService.Listing>> list(final Revision<?> revision,
                                                          final com.namazustudios.socialengine.rt.Path path) {

        final PathMapping mapping = new PathMapping(path);

        return utils.doOperation(() -> {

            final Stream<ResourceService.Listing> listings = Files
                    .walk(mapping.fsPath)
                    .filter(Files::isDirectory)
                    .map(directory -> loadRevisionListing(mapping, revision))
                    .filter(optional -> optional.isPresent() && !optional.get().isTombstone())
                    .map(optional -> optional.get());

            return revisionFactory.create(revision, listings);

        });

    }

    public Optional<RevisionListing> loadRevisionListing(final com.namazustudios.socialengine.rt.Path path,
                                                         final Revision<?> revision) {
        final PathMapping pathMapping = new PathMapping(path);
        return loadRevisionListing(pathMapping, revision);
    }

    public Optional<RevisionListing> loadRevisionListing(final PathMapping mapping, final Revision<?> revision) {
        return utils.getFileForRevision(mapping.fsPath, revision).getValue().map(file -> utils.doOperation(() ->{

            garbageCollector.pin(mapping.fsPath, revision);

            try (final FileChannel fc = FileChannel.open(mapping.fsPath, READ)) {

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

    public class PathMapping {

        private final Path fsPath;

        private final com.namazustudios.socialengine.rt.Path rtPath;

        private PathMapping(final com.namazustudios.socialengine.rt.Path rtPath) {

            if (rtPath.hasContext()) {

                final NodeId nodeId = NodeId.nodeIdFromString(rtPath.getContext());

                if (!nodeId.equals(UnixFSPathIndex.this.nodeId)) {
                    throw new IllegalArgumentException(
                        "Path context does not contain expected node id: " + nodeId + ". " +
                        "Expected Node ID: " + UnixFSPathIndex.this.nodeId);
                }

                this.rtPath = rtPath;
            } else {
                this.rtPath = rtPath.toPathWithContext(nodeId.asString());
            }

            final Path relative = Paths.get(rtPath.toFileSystemPathString());
            this.fsPath = utils.getPathStorageRoot().resolve(relative).toAbsolutePath();

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PathMapping)) return false;
            PathMapping that = (PathMapping) o;
            return rtPath.equals(that.rtPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rtPath);
        }

    }

    public class RevisionListing implements ResourceService.Listing {

        private final PathMapping mapping;

        private final ResourceId resourceId;

        private final UnixFSObjectHeader objectHeader;

        private RevisionListing(final PathMapping mapping,
                               final ResourceId resourceId,
                               final UnixFSObjectHeader objectHeader) {
            this.mapping = mapping;
            this.resourceId = resourceId;
            this.objectHeader = objectHeader;
        }

        @Override
        public com.namazustudios.socialengine.rt.Path getPath() {
            return mapping.rtPath;
        }

        @Override
        public ResourceId getResourceId() {
            return resourceId;
        }

        public boolean isTombstone() {
            return objectHeader.tombstone.get();
        }

    }

    private class PathRevisionMap implements RevisionMap<com.namazustudios.socialengine.rt.Path, ResourceId> {

        @Override
        public Revision<ResourceId> getValueAt(final Revision<ResourceId> revision,
                                               final com.namazustudios.socialengine.rt.Path key) {
            final PathMapping mapping = new PathMapping(key);
            final Optional<RevisionListing> optionalRevisionListing = loadRevisionListing(mapping, revision);
            return revisionFactory.createOptional(revision, optionalRevisionListing.map(l -> l.getResourceId()));
        }

    }

}
