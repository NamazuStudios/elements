package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.Path.fromComponents;
import static com.namazustudios.socialengine.rt.id.NodeId.nodeIdFromString;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.DIRECTORY_SUFFIX;
import static java.lang.String.format;
import static java.nio.file.Files.readSymbolicLink;
import static java.util.stream.Collectors.joining;

public class UnixFSPathMapping {

    private final UnixFSUtils utils;

    private final Path fsPath;

    private final com.namazustudios.socialengine.rt.Path rtPath;

    public UnixFSPathMapping(final UnixFSUtils utils,
                             final Path fsPath, final com.namazustudios.socialengine.rt.Path rtPath) {
        this.utils = utils;
        this.fsPath = fsPath;
        this.rtPath = rtPath;
    }

    /**
     * Returns the {@link com.namazustudios.socialengine.rt.Path} representing the symbolic path. This is always a fully
     * qualified path with the context set properly.
     *
     * @return the {@link com.namazustudios.socialengine.rt.Path} instance
     */
    public com.namazustudios.socialengine.rt.Path getPath() {
        return rtPath;
    }

    /**
     * Returns the filesystem path directory for the associated {@link com.namazustudios.socialengine.rt.Path}.
     *
     * @return the FS path.
     */
    public Path getPathDirectory() {
        return fsPath;
    }

    /**
     * Given the {@link Revision<?>} this will resolve the path to the supplied revision for the symbolic link.
     *resolveRevisionPath
     * @param revision the revision
     * @return the resolved path
     */
    public Path resolveLinkPath(final Revision<?> revision) {
        return utils.resolveLinkPath(fsPath, revision);
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
     * {@link com.namazustudios.socialengine.rt.Path} mapping.
     *
     * @param utils the {@link UnixFSUtils} used to determine the actual configured location
     * @param nodeId the {@link NodeId} to use a context
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path} representing the symbolic location of the resource
     * @return the {@link UnixFSPathMapping} instance
     */
    public static UnixFSPathMapping fromPath(final UnixFSUtils utils,
                                             final NodeId nodeId,
                                             final com.namazustudios.socialengine.rt.Path rtPath) {

        final com.namazustudios.socialengine.rt.Path fqPath;

        if (rtPath.hasContext()) {

            final NodeId contextNodeId = nodeIdFromString(rtPath.getContext());

            if (!contextNodeId.equals(nodeId)) {
                throw new IllegalArgumentException(
                    "Path context does not contain expected node id: " + nodeId + ". " +
                    "Expected Node ID: " + nodeId);
            }

            fqPath = rtPath;

        } else {
            fqPath = rtPath.toPathWithContext(nodeId.asString());
        }

        final Path relative = utils.resolvePathStorageRoot(nodeId).resolve(fqPath
            .getComponents()
            .stream()
            .map(component -> format("%s.%s", component, DIRECTORY_SUFFIX))
            .collect(joining(utils.getPathSeparator())));

        final Path fsPath = utils.getPathStorageRoot().resolve(relative).toAbsolutePath();
        return new UnixFSPathMapping(utils, fsPath, fqPath);

    }

    /**
     * Chases the following {@link Path} to a symbolic link and determines the
     * {@link com.namazustudios.socialengine.rt.Path} associated with it.
     *
     * @param utils the {@link UnixFSUtils} instance
     * @param symlink a {@link Path} that represents a symbolic link
     *
     * @return the {@link UnixFSPathMapping}
     */
    public static UnixFSPathMapping fromSymlinkPath(final UnixFSUtils utils, final NodeId nodeId, final Path symlink) {
        return utils.doOperation(() -> {

            final int dirExtensionLength = DIRECTORY_SUFFIX.length() + 1;

            final Path fsPath = readSymbolicLink(symlink).toAbsolutePath();
            final Path relative = utils.resolvePathStorageRoot(nodeId).relativize(fsPath);

            final List<String> components = new ArrayList<>();

            relative.forEach(p -> {
                String component;
                component = p.toString();
                component = component.substring(0, component.length() - dirExtensionLength);
                components.add(component);
            });

            final com.namazustudios.socialengine.rt.Path rtPath = new com.namazustudios.socialengine.rt.Path(
                nodeId.asString(),
                components
            );

            return new UnixFSPathMapping(utils, fsPath, rtPath);

        }, FatalException::new);
    }

}
