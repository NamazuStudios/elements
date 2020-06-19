package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;

import java.nio.file.Path;
import java.util.Objects;

public class UnixFSResourceIdMapping {

    private final UnixFSUtils utils;

    private final Path fsPath;

    private final ResourceId resourceId;

    private UnixFSResourceIdMapping(final UnixFSUtils utils, final ResourceId resourceId, final Path fsPath) {
        this.utils = utils;
        this.fsPath = fsPath;
        this.resourceId = resourceId;
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
     * Given the {@link Revision<?>} this will resolve the matching reverse-mapping directory.
     *
     * @param revision the revision directory
     *
     * @return the {@link Path} to the reverse-mapping directory
     */
    public Path resolveReverseDirectory(final NodeId nodeId, final Revision<?> revision) {
        return utils.resolveRevisionDirectoryPath(fsPath, revision).resolve(nodeId.asString());
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
