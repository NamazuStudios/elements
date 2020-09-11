package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_HARD_LINK;

public class UnixFSResourceIdMapping {

    private final UnixFSUtils utils;

    private final Path fsPath;

    private final ResourceId resourceId;

    private UnixFSResourceIdMapping(final UnixFSUtils utils, final ResourceId resourceId, final Path fsPath) {
        this.utils = utils;
        this.fsPath = fsPath.toAbsolutePath().normalize();
        this.resourceId = resourceId;
    }

    /**
     * Gets the {@link ResourceId} associated with this mapping.
     *
     * @return the {@link ResourceId}
     */
    public ResourceId getResourceId() {
        return resourceId;
    }

    /**
     * Returns the filesystem {@link Path} to the resource id directory.
     *
     * @return the {@link ResourceId} directory
     */
    public Path getResourceIdDirectory() {
        return fsPath;
    }

    /**
     * Given the {@link Revision<?>} this will resolve the path to the supplied {@link Revision<?>}.
     *
     * @param revision the {@link Revision<?>}
     * @return the {@link Path} to the revision
     */
    public Path resolveRevisionFilePath(final Revision<?> revision) {
        return utils.resolveRevisionFilePath(fsPath, revision);
    }

    /**
     * Will return the root of all reverse-mapping directories.
     *
     * @return the {@link Path} to the reverse-mapping directory
     */
    public Path resolveReverseDirectories() {
        return utils.resolveRevisionDirectoryPath(fsPath);
    }

    /**
     * Finds the the tombstone file, if it exists.
     *
     * @param revision the {@link Revision<?>} to check
     *
     * @return the {@link Revision<Path>} of the tombstone file.
     */
    public Revision<Path> findTombstone(final Revision<?> revision) {
        return utils.findLatestTombstone(fsPath, revision, REVISION_HARD_LINK);
    }

    /**
     * Finds the latest revision for this {@link UnixFSResourceIdMapping}, if it exists.
     *
     * @param revision the revision to find
     *
     * @return the {@link Revision<Path>} containing the results
     */
    public Revision<Path> findLatestRevision(final Revision<?> revision) {
        return utils.findLatestForRevision(fsPath, revision, REVISION_HARD_LINK);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnixFSResourceIdMapping that = (UnixFSResourceIdMapping) o;
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

    /**
     * Creates a {@link UnixFSResourceIdMapping} from the {@link UnixFSUtils} and a string representing the
     * {@link ResourceId}. This will appropriately resolve the directory from the configuration supplied in the utils.
     *
     * @param utils the utils instance which contains the necessary configuration
     * @param resourceIdString the {@link ResourceId} string for which to associate w/ the mapping
     *
     * @return the {@link UnixFSResourceIdMapping}
     */
    public static UnixFSResourceIdMapping fromResourceId(final UnixFSUtils utils, final String resourceIdString) {
        final ResourceId resourceId = ResourceId.resourceIdFromString(resourceIdString);
        return fromResourceId(utils, resourceId);
    }

    /**
     * Creates a {@link UnixFSResourceIdMapping} from the {@link UnixFSUtils} and the {@link ResourceId}. This will
     * appropriately resolve the directory from the configuration supplied in the utils.
     *
     * @param utils the utils instance which contains the necessary configuration
     * @param resourceId the {@link ResourceId} for which to associate w/ the mapping
     *
     * @return the {@link UnixFSResourceIdMapping}
     */
    public static UnixFSResourceIdMapping fromResourceId(final UnixFSUtils utils, final ResourceId resourceId) {

        final Path fsPath = utils
            .getResourceStorageRoot()
            .resolve(resourceId.asString())
            .toAbsolutePath();

        return new UnixFSResourceIdMapping(utils, resourceId, fsPath);

    }

}
