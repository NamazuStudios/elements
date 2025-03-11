package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class UnixFSTaskPathMapping implements UnixFSHasFilesystemPath {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSPathMapping.class);

    private final ResourceId resourceId;

    private final UnixFSUtils unixFSUtils;

    private final Path filesystemPath;

    private UnixFSTaskPathMapping(
            final UnixFSUtils unixFSUtils,
            final Path filesystemPath,
            final ResourceId resourceId) {
        unixFSUtils.check(filesystemPath);
        this.unixFSUtils = unixFSUtils;
        this.filesystemPath = filesystemPath;
        this.resourceId = resourceId;
    }

    /**
     * Gets the {@link UnixFSTaskPathMapping} from the supplied {@link Path} and {@link UnixFSUtils}.
     *
     * @param unixFSUtils the utils
     * @param taskPath the {@link Path}
     * @return the {@link UnixFSTaskPathMapping}
     */
    public static UnixFSTaskPathMapping fromFSPath(final UnixFSUtils unixFSUtils, final Path taskPath) {

        final var resourceIdString = unixFSUtils
                .stripExtension(taskPath)
                .getFileName()
                .toString();

        final var resourceId = ResourceId.resourceIdFromString(resourceIdString);
        return new UnixFSTaskPathMapping(unixFSUtils, taskPath, resourceId);

    }

    /**
     * Gets a task mapping for the supplied file.
     *
     * @param unixFSUtils the {@link UnixFSUtils} instance to use
     * @param resourceId the {@link ResourceId} which houses the tasks for the resource
     * @return the {@link UnixFSTaskPathMapping}
     */
    public static UnixFSTaskPathMapping fromResourceId(final UnixFSUtils unixFSUtils, final ResourceId resourceId) {

        final var basePath = unixFSUtils
                .resolveTaskStorageRoot(resourceId)
                .resolve(resourceId.asString());

        final var taskPath = unixFSUtils.appendTaskExtension(basePath);
        return new UnixFSTaskPathMapping(unixFSUtils, taskPath, resourceId);

    }

    /**
     * Gets the {@link TaskId}.
     *
     * @return the {@link TaskId}
     */
    public ResourceId getResourceId() {
        return resourceId;
    }

    /**
     * Gets the {@link Path} to the task file.
     *
     * @return the {@link Path} to the task file
     */
    @Override
    public Path getFilesystemPath() {
        return filesystemPath;
    }

    /**
     * The {@link UnixFSUtils} as part of htis mapping.
     *
     * @return the {@link UnixFSUtils}
     */
    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

}
