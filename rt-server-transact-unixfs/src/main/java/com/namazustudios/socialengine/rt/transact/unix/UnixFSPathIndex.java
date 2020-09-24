package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.PathIndex;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionMap;
import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_SYMBOLIC_LINK;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
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

            final Stream<ResourceService.Listing> listings;

            if (isDirectory(mapping.getPathDirectory())) {
                listings = rtPath.isWildcard() ? recursiveListing(mapping, nodeId, revision) :
                                                 singularListing(mapping, nodeId, revision);
            } else {
                listings = Stream.empty();
            }

            return revision.withValue(listings);

        });

    }

    private Stream<ResourceService.Listing> singularListing(
            final UnixFSPathMapping mapping,
            final NodeId nodeId,
            final Revision<?> revision) throws IOException {
        // TODO Fix This
        return Stream.empty();
//        return loadRevisionListing(nodeId, revision, mapping.getPathDirectory())
//            .map(rl -> Stream.of((ResourceService.Listing)rl))
//            .orElseGet(() -> (Stream<ResourceService.Listing>) Stream.empty());
    }

    private Stream<ResourceService.Listing> recursiveListing(
            final UnixFSPathMapping mapping,
            final NodeId nodeId,
            final Revision<?> revision) throws IOException {
        return Files
            .walk(mapping.getPathDirectory())
            .filter(p -> isDirectory(p, NOFOLLOW_LINKS))
            .map(directory -> loadRevisionListing(nodeId, revision, directory))
            .filter(optional -> optional.isPresent())
            .map(optional -> optional.get());
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

                            final Path parent = pinned.getParent();
                            final Path resourceDirectory = parent.resolve(readSymbolicLink(pinned));

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

    public void addPath(final Revision<?> revision,
                        final com.namazustudios.socialengine.rt.Path path) {
        final UnixFSReversePathMapping reversePathMapping = UnixFSReversePathMapping.fromRTPath(utils, path);
        final Path reversePathForNodeId = reversePathMapping.resolveReverseDirectory();
        utils.doOperationV(() -> createDirectories(reversePathForNodeId), FatalException::new);
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
            final Path pathSymlinkTarget = pathDirectory.relativize(resourceIdDirectory);

            logger.trace("Creating directory {}", pathDirectory);
            createDirectories(pathDirectory);

            logger.trace("Creating symlink {} -> {}", pathSymlink, pathSymlinkTarget);
            createSymbolicLink(pathSymlink, pathSymlinkTarget);

            logger.trace("Created symlink {} -> {}", pathSymlink, pathSymlinkTarget);

        }, FatalException::new);

    }

    public void linkReverse(final Revision<?> revision,
                            final NodeId nodeId,
                            final ResourceId resourceId,
                            final com.namazustudios.socialengine.rt.Path rtPath) {

        final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);
        final UnixFSReversePathMapping reversePathMapping = UnixFSReversePathMapping.fromRTPath(utils, nodeId, rtPath);

        utils.doOperationV(() -> {

            final Path reverseResourceIdDirectory = reversePathMapping.resolveReverseDirectory(resourceId);
            createDirectories(reverseResourceIdDirectory);

            final Revision<Path> latest = reversePathMapping.findLatestSymlink(revision, resourceId);

            final Path linkDirectory;

            if (Revision.ZERO.compareTo(latest) == 0) {

                // No revision exists at all and we need to make sure that the directory exists

                final Path symlink = reversePathMapping.resolveSymlink(revision, resourceId);
                linkDirectory = reverseResourceIdDirectory.resolve(randomUUID().toString());

                // Creates the directory to hold the contents of the link
                createDirectories(linkDirectory);
                createSymbolicLink(symlink, linkDirectory.getFileName());

            } else if (latest.compareTo(revision) > 0) {

                // A revision exists, but it is older. So we need to make the symbolic link to a new revision
                // and copy the existing directory over.

                final Path symlink = reversePathMapping.resolveSymlink(revision, resourceId);
                linkDirectory = reverseResourceIdDirectory.resolve(randomUUID().toString());

                createDirectories(linkDirectory);
                createSymbolicLink(symlink, linkDirectory.getFileName());

                final Path olderRevision = readSymbolicLink(latest.getValue().get());

                walk(olderRevision).forEach(
                    source ->
                    utils.doOperationV(() ->
                    copy(source, olderRevision.resolve(olderRevision.relativize(source)))));

            } else if (latest.compareTo(revision) == 0) {

                // We have already prepared the reverse directory, so it is now a simple matter of just using the
                // existing symbolic link

                final Path symlink = reversePathMapping.resolveSymlink(revision, resourceId);
                linkDirectory = readSymbolicLink(symlink);

            } else {
                throw new FatalException("Invalid revision found: " + latest);
            }

            // At long last we have a link directory we can actually make the reverse mapping. The mapping will have
            // a randomly assigned UUID for the symbolic link.

            final Path link = linkDirectory.resolve(randomUUID().toString());
            final Path target = linkDirectory.relativize(pathMapping.getPathDirectory());
            createSymbolicLink(link, target);

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

            final UnixFSReversePathMapping reversePathMapping = UnixFSReversePathMapping.fromNodeId(utils, nodeId);
            final Path reverseDirectory = reversePathMapping.resolveReverseDirectory(key);

            if (!exists(reverseDirectory)) return revision.withOptionalValue(Optional.empty());

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

}
