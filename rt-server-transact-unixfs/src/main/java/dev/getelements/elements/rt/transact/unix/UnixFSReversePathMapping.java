package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.nio.file.Path;

public class UnixFSReversePathMapping implements UnixFSHasFilesystemPath {

    private final UnixFSUtils unixFSUtils;

    private final Path fsPath;

    private final ResourceId resourceId;

    private UnixFSReversePathMapping(final UnixFSUtils unixFSUtils, final Path fsPath, final ResourceId resourceId) {
        this.unixFSUtils = unixFSUtils;
        this.fsPath = fsPath;
        this.resourceId = resourceId;
    }

    @Override
    public Path getFilesystemPath() {
        return fsPath;
    }

    /**
     * Creates a {@link UnixFSReversePathMapping} from the {@link UnixFSUtils} and the {@link ResourceId}. This will
     * appropriately resolve the directory from the configuration supplied in the utils.
     *
     * @param utils the utils instance which contains the necessary configuration
     * @param resourceId the {@link ResourceId} for which to associate w/ the mapping
     *
     * @return the {@link UnixFSResourceContentsMapping}
     */
    public static UnixFSReversePathMapping fromResourceId(final UnixFSUtils utils, final ResourceId resourceId) {

        final var fsPath = utils
                .resolveReversePathStorageRoot(resourceId)
                .resolve(resourceId.asString())
                .toAbsolutePath();

        final var fsPathWithExtension = utils.appendReversePathExtension(fsPath);
        return new UnixFSReversePathMapping(utils, fsPathWithExtension, resourceId);

    }

}
