package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.nio.file.Path;

import static java.lang.String.format;

public class UnixFSResourceContentsMapping implements UnixFSHasFilesystemPath {

    private final UnixFSUtils utils;

    private final Path fsPath;

    private final ResourceId resourceId;

    private UnixFSResourceContentsMapping(final UnixFSUtils utils, final ResourceId resourceId, final Path fsPath) {
        this.utils = utils;
        this.fsPath = fsPath.toAbsolutePath().normalize();
        this.resourceId = resourceId;
    }

    /**
     * Gets the {@link UnixFSUtils} associated with this mapping.
     *
     * @return the {@link UnixFSUtils}
     */
    public UnixFSUtils getUtils() {
        return utils;
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
     * @return the {@link ResourceId} file
     */
    @Override
    public Path getFilesystemPath() {
        return fsPath;
    }

    @Override
    public String toString() {
        return "PathMapping{" +
                "fsPath=" + fsPath +
                ", resourceId=" + resourceId +
                '}';
    }

    /**
     * Creates a {@link UnixFSResourceContentsMapping} from the {@link UnixFSUtils} and the {@link ResourceId}. This will
     * appropriately resolve the directory from the configuration supplied in the utils.
     *
     * @param utils the utils instance which contains the necessary configuration
     * @param resourceId the {@link ResourceId} for which to associate w/ the mapping
     *
     * @return the {@link UnixFSResourceContentsMapping}
     */
    public static UnixFSResourceContentsMapping fromResourceId(final UnixFSUtils utils, final ResourceId resourceId) {

        final var fsPath = utils
            .resolveResourceStorageRoot(resourceId)
            .resolve(resourceId.asString())
            .toAbsolutePath();

        final var fsPathWithExtension = utils.appendResourceExtension(fsPath);
        return new UnixFSResourceContentsMapping(utils, resourceId, fsPathWithExtension);

    }

}
