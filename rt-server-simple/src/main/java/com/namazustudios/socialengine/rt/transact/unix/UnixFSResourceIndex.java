package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.ResourceIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;

public class UnixFSResourceIndex implements ResourceIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSResourceIndex.class);

    private final NodeId nodeId;

    private final UnixFSUtils utils;

    private final RevisionFactory revisionFactory;

    private final UnixFSPathIndex unixFSPathIndex;

    private final UnixFSGarbageCollector garbageCollector;

    @Inject
    public UnixFSResourceIndex(
            final NodeId nodeId,
            final UnixFSUtils utils,
            final RevisionFactory revisionFactory,
            final UnixFSPathIndex unixFSPathIndex,
            final UnixFSGarbageCollector garbageCollector) {
        this.nodeId = nodeId;
        this.utils = utils;
        this.revisionFactory = revisionFactory;
        this.unixFSPathIndex = unixFSPathIndex;
        this.garbageCollector = garbageCollector;
    }

    @Override
    public Revision<Boolean> existsAt(final Revision<?> revision, final ResourceId resourceId) {
        final Path resourceIdDirectory = utils.getResourceStorageRoot().resolve(resourceId.asString());
        return utils.getFileForRevision(resourceIdDirectory, revision).map(f -> true);
    }

    @Override
    public Revision<ReadableByteChannel> loadResourceContentsAt(
            final Revision<?> revision,
            final com.namazustudios.socialengine.rt.Path path) {

        final Optional<ReadableByteChannel> readableByteChannelOptional = unixFSPathIndex
            .loadRevisionListing(path, revision)
            .filter(rv -> !rv.isTombstone())
            .map(rv -> load(revision, rv.getResourceId()));

        return revisionFactory.createOptional(revision, readableByteChannelOptional);

    }

    @Override
    public Revision<ReadableByteChannel> loadResourceContentsAt(
            final Revision<?> revision,
            final ResourceId resourceId) {
        final Path resourceIdDirectory = utils.getResourceStorageRoot().resolve(resourceId.asString());
        return utils.getFileForRevision(resourceIdDirectory, revision).map(file -> load(file, revision));
    }

    private ReadableByteChannel load(final Revision<?> revision, final ResourceId resourceId) {

        final PathMapping mapping = new PathMapping(resourceId);
        final Revision<Path> resourceFilePath = utils.getFileForRevision(mapping.fsPath, revision);

        return resourceFilePath.getValue()
            .map(file -> load(file, revision))
            .orElseThrow(() -> new ResourceNotFoundException());

    }

    private ReadableByteChannel load(final Path file, final Revision<?> revision) {

        FileChannel fc = null;

        try {

            final FileChannel out = fc = open(file, READ);
            garbageCollector.pin(file, revision);

            final UnixFSResourceHeader resourceHeader = new UnixFSResourceHeader();

            fc.read(resourceHeader.getByteBuffer());
            if (resourceHeader.tombstone.get()) throw new ResourceNotFoundException();

            fc = null;
            return out;

        } catch (FileNotFoundException ex) {
            throw new ResourceNotFoundException(ex);
        } catch (IOException ex) {
            throw new InternalException(ex);
        } finally {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException ex) {
                    logger.error("Unable to close file channel - {}", file, ex);
                }
            }
        }

    }

    private class PathMapping {

        private final Path fsPath;

        private final ResourceId resourceId;

        private PathMapping(ResourceId resourceId) {
            this.resourceId = resourceId;
            this.fsPath = utils
                .getResourceStorageRoot()
                .resolve(resourceId.asString())
                .toAbsolutePath();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathMapping that = (PathMapping) o;
            return resourceId.equals(that.resourceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceId);
        }

        @Override
        public String toString() {
            return "PathMapping{" +
                    "fsPath=" + fsPath +
                    ", resourceId=" + resourceId +
                    '}';
        }

    }

}
