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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_SYMBOLIC_LINK;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;

public class UnixFSPathIndex implements PathIndex {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSPathIndex.class);

    private final UnixFSUtils utils;

    private final UnixFSGarbageCollector garbageCollector;

    @Inject
    public UnixFSPathIndex(
            final UnixFSUtils utils,
            final UnixFSGarbageCollector garbageCollector) {
        this.utils = utils;
        this.garbageCollector = garbageCollector;
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

        final UnixFSPathMapping mapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);

        return utils.doOperation(() -> {

            final Stream<ResourceService.Listing> listings = Files
                .walk(mapping.getPathDirectory())
                .filter(Files::isDirectory)
                .map(directory -> loadRevisionListing(nodeId, revision, directory))
                .filter(optional -> optional.isPresent())
                .map(optional -> optional.get());

            return revision.withValue(listings);

        });

    }

    public Optional<RevisionListing> loadRevisionListing(final NodeId nodeId,
                                                         final Revision<?> revision,
                                                         final Path directory) {

        final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRelativeFSPath(utils, nodeId, directory);

        return utils.findLatestForRevision(pathMapping.getPathDirectory(), revision, REVISION_SYMBOLIC_LINK)
                    .getValue()
                    .map(file -> (RevisionListing) utils.doOperation(() -> {

                            final Path pinned = garbageCollector.pin(file, revision);

                            if (utils.isTombstone(pinned)) {
                                // Totally expected behavior. A previous revision deleted the path but it hasn't been
                                // collected yet by the garbage collector. We just filter this one out.
                                return Optional.empty();
                            }

                            final Path resourceDirectory = readSymbolicLink(pinned);

                            if (!isDirectory(resourceDirectory)) {
                                // This should not happen if the garbage collector is doing its job properly.
                                logger.warn("Found dead symbolic link {}.", resourceDirectory.toAbsolutePath());
                                return Optional.empty();
                            }

                            final String resourceIdString = resourceDirectory.getFileName().toString();
                            final ResourceId resourceId = ResourceId.resourceIdFromString(resourceIdString);
                            return new RevisionListing(pathMapping, resourceId);

                        }
                    )
                );
    }

    public void link(final Revision<?> revision,
                     final NodeId nodeId,
                     final ResourceId resourceId,
                     final com.namazustudios.socialengine.rt.Path rtPath) {

        final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);
        final UnixFSResourceIdMapping resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

        if (!isDirectory(resourceIdMapping.getResourceIdDirectory())) {

            // This should not happen because it should have been pre-checked before even attempting this operation
            // if this does happen, it indicates that there was some data loss and the data store is corrupt or
            // has been tampered with since having been used.

            final String msg = format("Attempting to link non-existent directory %s -> %s",
                    resourceIdMapping.getResourceIdDirectory(),
                    rtPath);

            logger.error("Attempting to link non-existent directory {} -> {}",
                    resourceIdMapping.getResourceIdDirectory(),
                    rtPath);

            throw new FatalException(msg);

        }

        final Revision<Path> tombstonePathRevision = resourceIdMapping.findTombstone(revision);

        if (tombstonePathRevision.getValue().isPresent()) {

            // Same as above, this should have been pre-checked. This means that there was an improper pre-check
            // but in this case the problem is that the file exists it was deleted at some point in the past.

            final String msg = format("Attempting to link tombstoned directory %s -> %s",
                    resourceIdMapping.getResourceIdDirectory(),
                    rtPath);

            logger.error("Attempting to link tombstoned directory {} -> {}",
                    resourceIdMapping.getResourceIdDirectory(),
                    rtPath);

            throw new FatalException(msg);

        }

        utils.doOperationV(() -> {

            final Path pathDirectory = pathMapping.getPathDirectory();
            final Path resourceIdDirectory = resourceIdMapping.getResourceIdDirectory();

            final Path pathSymlink = utils.resolveSymlinkPath(pathDirectory, revision);
            final Path pathSymlinkTarget = pathSymlink.relativize(resourceIdDirectory);

            logger.trace("Creating directory {}", pathDirectory);
            createDirectories(pathDirectory);

            final Path reversePathDirectoryParent = resourceIdMapping.resolveReverseDirectory(nodeId);
            final Path reversePathDirectory = utils
                .findLatestForRevision(reversePathDirectoryParent, revision, REVISION_SYMBOLIC_LINK)
                .map(symlink -> garbageCollector.pin(symlink, revision))
                .map(symlink -> utils.doOperation(() -> readSymbolicLink(symlink)))
                .getValue()
                .orElseGet(() -> utils.doOperation(() -> {

                    // No mapping exists for this revision because it can't find the symlink. Therefore, we must make
                    // the new directory and setup all the links.

                    // The link is determined by the revision number unique ID
                    final Path link = utils.resolveSymlinkPath(reversePathDirectoryParent, revision);

                    // The target is a randomly generated UUID.
                    final Path target = reversePathDirectoryParent.resolve(randomUUID().toString());

                    logger.trace("Creating directory {}", target);
                    logger.trace("Creating symbolic link {} -> {} for new reverse mapping.", link, target);

                    createDirectories(target);
                    createSymbolicLink(link, target.getFileName());

                    return target;

                }));

                final Path reversePathSymlink = reversePathDirectory.resolve(randomUUID().toString());
                final Path reversePathSymlinkTarget = reversePathSymlink.relativize(pathDirectory);

                logger.trace("Creating symlink {} -> {}", pathSymlink, pathSymlinkTarget);
                logger.trace("Creating symlink {} -> {}", reversePathSymlink, reversePathSymlinkTarget);

                createSymbolicLink(pathSymlink, pathSymlinkTarget);
                createSymbolicLink(reversePathSymlink, reversePathSymlinkTarget);

        }, FatalException::new);

    }

    public void unlink(final Revision<?> revision,
                       final NodeId nodeId,
                       final com.namazustudios.socialengine.rt.Path rtPath) {
        final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);
        garbageCollector.getUtils().tombstone(pathMapping.getPathDirectory(), revision);
    }

    private class RevisionListing implements ResourceService.Listing {

        private final UnixFSPathMapping mapping;

        private final ResourceId resourceId;

        private RevisionListing(final UnixFSPathMapping mapping,
                                final ResourceId resourceId) {
            this.mapping = mapping;
            this.resourceId = resourceId;
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
            final UnixFSPathMapping mapping = UnixFSPathMapping.fromRTPath(utils, nodeId, key);
            if (!exists(mapping.getPathDirectory())) return revision.withOptionalValue(Optional.empty());

            return utils
                .findLatestForRevision(mapping.getPathDirectory(), revision, REVISION_SYMBOLIC_LINK)
                .map(symlink -> utils.doOperation(() -> {
                    final Path revisionDirectory = readSymbolicLink(symlink);
                    final String fileName = revisionDirectory.getFileName().toString();
                    final UnixFSResourceIdMapping resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils,fileName);
                    return resourceIdMapping.getResourceId();
                }, FatalException::new));
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
            if (!exists(resourceIdMapping.getResourceIdDirectory())) return revision.withOptionalValue(Optional.empty());

            final Path reverseDirectory = resourceIdMapping.resolveReverseDirectory(nodeId);

            final Set<com.namazustudios.socialengine.rt.Path> pathSet =
                utils.findLatestForRevision(reverseDirectory, revision, REVISION_SYMBOLIC_LINK)
                     .map(symlink -> garbageCollector.pin(symlink, revision))
                     .map(symlink -> utils.doOperation(() -> readSymbolicLink(symlink)))
                     .map(directory -> utils.doOperation(() -> Files.list(directory)))
                     .map(symlinkStream -> symlinkStream
                         .filter(path -> utils.doOperation(() -> isSymbolicLink(path)))
                         .map(symlink -> UnixFSPathMapping.fromFullyQualifiedSymlinkPath(utils, symlink))
                         .map(mapping -> mapping.getPath())
                         .collect(toSet())
                      ).getValue().orElseGet(Collections::emptySet);

            return revision.withValue(pathSet);

        }

    }

    public static void main(String[] args) throws Exception {

        // Keeping this here for reference
        final Path tmpdir = Paths.get(".", "tempdir");
        if (Files.isDirectory(tmpdir)) Files.walk(tmpdir).map(Path::toFile).forEach(File::delete);

        Files.createDirectories(tmpdir);

        final Path original = tmpdir.resolve("file").normalize();
        final Path target = Paths.get("file");
        final Path symlink0 = tmpdir.resolve("symlink0").toAbsolutePath().normalize();
        final Path symlink1 = tmpdir.resolve("symlink1").toAbsolutePath().normalize();

        Files.createFile(original);
        Files.createSymbolicLink(symlink0, target);
        Files.createLink(symlink1, symlink0);

    }

}
