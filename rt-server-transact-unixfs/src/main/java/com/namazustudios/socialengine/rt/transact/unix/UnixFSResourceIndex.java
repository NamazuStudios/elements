package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.ResourceIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.READ;

public class UnixFSResourceIndex implements ResourceIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSResourceIndex.class);

    private UnixFSUtils utils;

    private UnixFSGarbageCollector garbageCollector;

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(UnixFSUtils utils) {
        this.utils = utils;
    }

    public UnixFSGarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    @Inject
    public void setGarbageCollector(final UnixFSGarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    @Override
    public Revision<Boolean> existsAt(final Revision<?> revision, final ResourceId resourceId) {

        final UnixFSResourceIdMapping mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);
        if (!isDirectory(mapping.getResourceIdDirectory())) return Revision.zero();

        return utils.findLatestForRevision(
            mapping.getResourceIdDirectory(),
            revision,
            UnixFSUtils.LinkType.REVISION_HARD_LINK
        ).map(path -> getGarbageCollector().pin(path, revision))
         .map(path -> isRegularFile(path) && !utils.isTombstone(path));

    }

    @Override
    public Revision<ReadableByteChannel> loadResourceContentsAt(
            final Revision<?> revision,
            final ResourceId resourceId) throws IOException {

        final UnixFSResourceIdMapping mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);
        if (!isDirectory(mapping.getResourceIdDirectory())) return Revision.zero();

        final Revision<Path> pathRevision = utils.findLatestForRevision(
            mapping.getResourceIdDirectory(),
            revision,
            UnixFSUtils.LinkType.REVISION_HARD_LINK
        );

        if (!pathRevision.getValue().isPresent()) return Revision.zero();

        final ReadableByteChannel channel = load(pathRevision.getValue().get(), revision);
        return revision.withValue(channel);

    }

    private ReadableByteChannel load(final Path file, final Revision<?> revision) throws IOException {

        FileChannel fc = null;

        try {

            final Path pinned = getGarbageCollector().pin(file, revision);
            final FileChannel out = fc = open(pinned, READ);

            final UnixFSResourceHeader resourceHeader = new UnixFSResourceHeader();
            fc.read(resourceHeader.getByteBuffer());

            return out;

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

    /**
     * Removes the {@link ResourceId} from the the {@link UnixFSResourceIndex} by flagging the directory as a tombstone
     *
     *
     * @param revision
     * @param resourceId
     */
    public void removeResource(final Revision<?> revision, final ResourceId resourceId) {

        final UnixFSResourceIdMapping resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);
        getUtils().tombstone(resourceIdMapping.getResourceIdDirectory(), revision);

        final Path reverseRoot = resourceIdMapping.resolveReverseDirectories();

        utils.doOperationV(() ->
            list(reverseRoot).forEach(nodeIdDirectory -> tombstone(revision, nodeIdDirectory)),
            FatalException::new
        );

    }

    private void tombstone(final Revision<?> revision, final Path nodeIdDirectory) {
        final NodeId nodeId = NodeId.nodeIdFromString(nodeIdDirectory.getFileName().toString());
        utils.doOperationV(
            () -> list(nodeIdDirectory).forEach(symlink -> tombstone(revision, nodeId, symlink)),
            FatalException::new
        );
    }

    private void tombstone(final Revision<?> revision, final NodeId nodeId, final Path symbolicLink) {
        utils.doOperationV(() -> {
            final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRelativeFSPath(utils, nodeId, symbolicLink);
            getUtils().tombstone(pathMapping.getPathDirectory(), revision);
        }, FatalException::new);
    }

    @Override
    public void updateResource(final Revision<?> revision, final Path fsPath, final ResourceId resourceId) {

        final UnixFSResourceIdMapping mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

        utils.doOperationV(() -> {
            final Path revisionPath = mapping.resolveRevisionFilePath(revision);
            createLink(fsPath, revisionPath);
        }, FatalException::new);

    }

    /**
     * Links the supplied FS Path to the ResourceId.
     *
     * @param revision the revision to link
     * @param fsPath the fs path to link
     * @param resourceId the resource ID to link
     */
    public void linkNewResource(final Revision<?> revision, final Path fsPath, final ResourceId resourceId) {

        final UnixFSResourceIdMapping mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

        utils.doOperationV(() -> {
            final Path revisionPath = mapping.resolveRevisionFilePath(revision);
            createDirectories(mapping.getResourceIdDirectory());
            createLink(fsPath, revisionPath);
        }, FatalException::new);

    }

}
