package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.ResourceIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.NullResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.*;
import static java.lang.String.format;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardOpenOption.READ;

public class UnixFSResourceIndex implements ResourceIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSResourceIndex.class);

    private UnixFSUtils utils;

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(UnixFSUtils utils) {
        this.utils = utils;
    }

    @Override
    public Revision<Boolean> existsAt(final Revision<?> revision, final ResourceId resourceId) {

        final UnixFSResourceIdMapping mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);
        if (!isDirectory(mapping.getResourceIdDirectory())) return Revision.zero();

        return utils.findLatestForRevision(
            mapping.getResourceIdDirectory(),
            revision,
            REVISION_HARD_LINK
        ).map(path -> isRegularFile(path) && !utils.isTombstone(path));

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
            REVISION_HARD_LINK
        );

        if (!pathRevision.getValue().isPresent()) return Revision.zero();

        final Path revisionFilePath = pathRevision.getValue().get();

        if (getUtils().isNull(revisionFilePath))
            throw new NullResourceException();
        else if (getUtils().isTombstone(revisionFilePath))
            return Revision.zero();

        final ReadableByteChannel channel = open(revisionFilePath, READ);
        return revision.withValue(channel);

    }

    /**
     * Removes the {@link ResourceId} from the the {@link UnixFSResourceIndex} by flagging the directory as a tombstone.
     *
     * @param revision the revision at which to flag the {@link ResourceId} as removed
     * @param resourceId the {@link ResourceId} to remove
     */
    public void removeResource(final Revision<?> revision, final ResourceId resourceId) {

        final var resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

        final var resourceIdDirectory = getUtils()
            .findLatestForRevision(resourceIdMapping.getResourceIdDirectory(), revision, REVISION_HARD_LINK)
            .getValue()
            .map(p -> resourceIdMapping.getResourceIdDirectory())
            .orElseThrow(ResourceNotFoundException::new);

        getUtils().tombstone(resourceIdDirectory, revision, REVISION_HARD_LINK);

    }

    /**
     * Places a tombstone at the reverse mapping for the particular {@link ResourceId}.
     *
     * @param revision
     * @param resourceId
     */
    public void removeResourceReverseMappings(final Revision<?> revision, final ResourceId resourceId) {
        final var nodeId = resourceId.getNodeId();
        final var reversePathMapping = UnixFSReversePathMapping.fromNodeId(utils, nodeId);
        final var reverseDirectory = reversePathMapping.resolveReverseDirectory(resourceId);
        getUtils().tombstone(reverseDirectory, revision, DIRECTORY);
    }

    @Override
    public void updateResource(final Revision<?> revision, final Path fsPath, final ResourceId resourceId) {

        final var mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);
        final var absolute = getUtils().getStorageRoot().resolve(fsPath);

        utils.doOperationV(() -> {

            final var revisionPath = mapping.resolveRevisionFilePath(revision);

            if (exists(revisionPath)) {
                final var msg = format("%s does not match %s", revisionPath, fsPath);
                if (exists(fsPath) && !isSameFile(revisionPath, fsPath) ) throw new FatalException(msg);
            } else {
                createLink(revisionPath, absolute);
            }

        }, FatalException::new);

    }

    /**
     * Makes this {@link UnixFSResourceIndex} aware of a new {@link ResourceId}.
     *
     * @param revision the revision
     * @param resourceId the {@link ResourceId} to add
     */

    public void addResourceId(final Revision<?> revision, final ResourceId resourceId) {

        final UnixFSResourceIdMapping mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

        utils.doOperationV(() -> {
            createDirectories(mapping.getResourceIdDirectory());
            getUtils().markNull(mapping.getResourceIdDirectory(), revision, REVISION_HARD_LINK);
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

        final var mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);
        final var absolute = getUtils().getStorageRoot().resolve(fsPath);

        getUtils().doOperationV(() -> {

            final var revisionPath = mapping.resolveRevisionFilePath(revision);
            createDirectories(mapping.getResourceIdDirectory());

            if (exists(revisionPath)) {
                final var msg = format("%s does not match %s", revisionPath, fsPath);
                if (!isSameFile(revisionPath, fsPath)) throw new FatalException(msg);
            } else {
                createLink(revisionPath, absolute);
            }

        }, FatalException::new);

    }

}
