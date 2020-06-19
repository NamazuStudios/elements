package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.ResourceIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.createLink;
import static java.nio.file.StandardOpenOption.READ;

public class UnixFSResourceIndex implements ResourceIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSResourceIndex.class);

    private final UnixFSUtils utils;

    private final UnixFSGarbageCollector garbageCollector;

    @Inject
    public UnixFSResourceIndex(final UnixFSUtils utils, final UnixFSGarbageCollector garbageCollector) {
        this.utils = utils;
        this.garbageCollector = garbageCollector;
    }

    @Override
    public Revision<Boolean> existsAt(final Revision<?> revision, final ResourceId resourceId) {
        final Path resourceIdDirectory = utils.getResourceStorageRoot().resolve(resourceId.asString());
        return utils.findLatestForRevision(resourceIdDirectory, revision).map(f -> true);
    }

    @Override
    public Revision<ReadableByteChannel> loadResourceContentsAt(
            final NodeId nodeId,
            final Revision<?> revision,
            final com.namazustudios.socialengine.rt.Path rtPath) {
        final UnixFSPathMapping mapping = UnixFSPathMapping.fromPath(utils, nodeId, rtPath);
        return utils.findLatestForRevision(mapping.getPathDirectory(), revision).map(f -> load(f, revision));
    }

    @Override
    public Revision<ReadableByteChannel> loadResourceContentsAt(
            final Revision<?> revision,
            final ResourceId resourceId) {
        final Path resourceIdDirectory = utils.getResourceStorageRoot().resolve(resourceId.asString());
        return utils.findLatestForRevision(resourceIdDirectory, revision).map(file -> load(file, revision));
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

    /**
     * Removes the {@link ResourceId} from the the {@link UnixFSResourceIndex} by flagging the directory as a tombstone
     *
     *
     * @param revision
     * @param resourceId
     */
    public void removeResource(final Revision<?> revision, final ResourceId resourceId) {
        final UnixFSResourceIdMapping pathMapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);
        garbageCollector.tombstone(pathMapping.getResourceIdDirectory(), revision);
    }

    /**
     * Links the supplied FS Path to the ResourceId.
     *
     * @param revision the revision to link
     * @param fsPath the fs path to link
     * @param resourceId the resource ID to link
     */
    public void linkFSPathToResourceId(final Revision<?> revision, final Path fsPath, final ResourceId resourceId) {

        final UnixFSResourceIdMapping mapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

        utils.doOperationV(() -> {
            final Path revisionPath = mapping.resolveRevisionFilePath(revision);
            createLink(fsPath, revisionPath);
        }, FatalException::new);

    }

}
