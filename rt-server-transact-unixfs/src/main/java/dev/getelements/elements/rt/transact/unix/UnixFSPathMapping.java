package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.HasNodeId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

import static dev.getelements.elements.sdk.cluster.path.Path.fromPathString;
import static java.lang.String.format;

public abstract class UnixFSPathMapping implements UnixFSHasFilesystemPath {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSPathMapping.class);

    private final UnixFSUtils unixFSUtils;

    private final dev.getelements.elements.sdk.cluster.path.Path rtPath;

    private UnixFSPathMapping(final UnixFSUtils unixFSUtils,
                              final dev.getelements.elements.sdk.cluster.path.Path rtPath) {
        this.rtPath = rtPath;
        this.unixFSUtils = unixFSUtils;
    }

    /**
     * Get the {@link UnixFSUtils} associated with this {@link UnixFSPathMapping}.
     *
     * @return the utils for this mapping.
     */
    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

    /**
     * Returns the {@link dev.getelements.elements.sdk.cluster.path.Path} representing the symbolic path. This is always a fully
     * qualified path with the context set properly.
     *
     * @return the {@link dev.getelements.elements.sdk.cluster.path.Path} instance
     */
    public dev.getelements.elements.sdk.cluster.path.Path getPath() {
        return rtPath;
    }

    @Override
    public Path getFilesystemPath(final String transactionId) {

        final var components = rtPath.getComponents();

        if (components.isEmpty()) {
            throw  new IllegalArgumentException("Not suitable for root path.");
        }

        return UnixFSHasFilesystemPath.super.getFilesystemPath(transactionId);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnixFSPathMapping)) return false;
        UnixFSPathMapping that = (UnixFSPathMapping) o;
        return rtPath.equals(that.rtPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rtPath);
    }

    /**
     * Creates a {@link UnixFSPathMapping} with the supplied information which resolves a particular {@link Path} to a
     * {@link dev.getelements.elements.sdk.cluster.path.Path} mapping.
     *
     * @param utils the {@link UnixFSUtils} used to determine the actual configured location
     * @param rtPath the {@link dev.getelements.elements.sdk.cluster.path.Path} representing the symbolic location of the resource
     * @return the {@link UnixFSPathMapping} instance
     */
    public static UnixFSPathMapping fromRTPath(final UnixFSUtils utils,
                                               final dev.getelements.elements.sdk.cluster.path.Path rtPath) {
        final var nodeRoot = utils.resolvePathStorageRoot(rtPath);
        final var resourcePath = rtPath.toRelativeFilesystemPath();
        final var fullPath = nodeRoot.resolve(resourcePath);
        return new UnixFSPathMapping(utils, rtPath) {
            @Override
            public Path getFilesystemPath() {
                return utils.appendReversePathExtension(fullPath.toAbsolutePath().normalize());
            }
        };
    }

    /**
     * Creates a {@link UnixFSPathMapping} with the supplied information which resolves a particular {@link Path} to a
     * {@link dev.getelements.elements.sdk.cluster.path.Path} mapping. This assumes that the supplied {@link Path} is fully
     * qualified in that the first component contains a stringified {@link NodeId}.
     *
     * This relativizes this against the path storage root and then infers the {@link NodeId} from the first
     * component of the path.
     *
     * @param utils the {@link UnixFSUtils} used to determine the actual configured location
     * @param fsPath the {@link Path} representing the on-disk directory for the {@link dev.getelements.elements.sdk.cluster.path.Path}
     * @return the {@link UnixFSPathMapping} instance
     */
    public static UnixFSPathMapping fromFullyQualifiedFSPath(
            final UnixFSUtils utils,
            final HasNodeId hasNodeId,
            final Path fsPath) {

        final var stripped = utils.stripExtension(fsPath);

        final var relative = utils
                .resolvePathStorageRoot(hasNodeId)
                .relativize(stripped);

        final var nodeId = hasNodeId.getNodeId().getNodeId();
        final var rtPath = dev.getelements.elements.sdk.cluster.path.Path
                .fromPathString(relative.toString(), utils.getPathSeparator())
                .toPathWithContext(nodeId.toString());

        final var absoluteFsPath = utils.check(fsPath)
                .toAbsolutePath()
                .normalize();

        return new UnixFSPathMapping(utils, rtPath) {

            @Override
            public Path getFilesystemPath() {
                return absoluteFsPath;
            }

        };

    }

}
