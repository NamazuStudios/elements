package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.PathIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionFactory;
import com.namazustudios.socialengine.rt.transact.RevisionMap;
import com.namazustudios.socialengine.rt.util.LazyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Objects;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.Path.fromFileSystemPathString;
import static com.namazustudios.socialengine.rt.id.ResourceId.getSizeInBytes;
import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromByteBuffer;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionDataStore.STORAGE_ROOT_DIRECTORY;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.Comparator.reverseOrder;

public class UnixFSLinkPathIndex implements PathIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSLinkPathIndex.class);

    public static final String PATH_DIRECTORY = "path";

    private final NodeId nodeId;

    private final UnixFSUtils utils;

    private final RevisionFactory revisionFactory;

    private final UnixFSGarbageCollector garbageCollector;

    private final Path pathStorageRoot;

    @Inject
    public UnixFSLinkPathIndex(
            final NodeId nodeId,
            final UnixFSUtils utils,
            final RevisionFactory revisionFactory,
            final UnixFSGarbageCollector garbageCollector,
            @Named(STORAGE_ROOT_DIRECTORY) final Path storageRoot) throws IOException {
        this.nodeId = nodeId;
        this.utils = utils;
        this.revisionFactory = revisionFactory;
        this.garbageCollector = garbageCollector;
        this.pathStorageRoot = storageRoot.toAbsolutePath().resolve(PATH_DIRECTORY).toAbsolutePath();
    }

    @Override
    public RevisionMap<com.namazustudios.socialengine.rt.Path, ResourceId> getRevisionMap() {
        return null;
    }

    @Override
    public Revision<Stream<ResourceService.Listing>> list(final Revision<?> revision,
                                                          final com.namazustudios.socialengine.rt.Path path) {

        final PathMapping mapping = new PathMapping(path);

        return utils.doOperation(() -> {

            final Stream<ResourceService.Listing> listings = Files
                    .walk(mapping.fsPath)
                    .filter(Files::isDirectory)
                    .map(directory -> new RevisionPathMapping(directory, revision));

            return revisionFactory.create(revision, listings);

        });

    }

    private class PathMapping {

        protected final Path fsPath;

        protected final com.namazustudios.socialengine.rt.Path rtPath;

        public PathMapping(final Path fsPath) {
            this.fsPath = fsPath;
            this.rtPath = fromFileSystemPathString(fsPath.toString());
        }

        public PathMapping(final com.namazustudios.socialengine.rt.Path rtPath) {

            if (rtPath.hasContext()) {

                final NodeId nodeId = NodeId.nodeIdFromString(rtPath.getContext());

                if (!nodeId.equals(UnixFSLinkPathIndex.this.nodeId)) {
                    throw new IllegalArgumentException(
                        "Path context does not contain expected node id: " + nodeId + ". " +
                        "Expected Node ID: " + UnixFSLinkPathIndex.this.nodeId);
                }

                this.rtPath = rtPath;
            } else {
                this.rtPath = rtPath.toPathWithContext(nodeId.asString());
            }

            final Path relative = Paths.get(rtPath.toFileSystemPathString());
            this.fsPath = pathStorageRoot.toAbsolutePath().resolve(relative).toAbsolutePath();

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

    private class RevisionPathMapping extends PathMapping implements ResourceService.Listing {

        private final LazyValue<ResourceId> resourceId = new LazyValue<>(() -> utils.doOperation(() -> {
            try (final FileChannel fc = FileChannel.open(fsPath, READ)) {
                final ByteBuffer buffer = ByteBuffer.allocate(getSizeInBytes());
                fc.read(buffer);
                buffer.rewind();
                return resourceIdFromByteBuffer(buffer);
            }
        }));

        public RevisionPathMapping(final Path fsPath, final Revision<?> revision) {
            super(fsPath);
            final Path file = utils.resolve(fsPath, revision);
            garbageCollector.pin(file, revision);
        }

        @Override
        public com.namazustudios.socialengine.rt.Path getPath() {
            return rtPath;
        }

        @Override
        public ResourceId getResourceId() {
            return resourceId.get();
        }

    }

    private static void main(String[] args) throws Exception {

        final Path root = Paths.get("paths");
        final Path test = Paths.get("foo/bar/baz");
        final Path absolute = root.toAbsolutePath().resolve(test).toAbsolutePath();

        try {
            Files.createDirectories(absolute);
        } finally {
            Files.walk(root)
                 .sorted(reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

}
