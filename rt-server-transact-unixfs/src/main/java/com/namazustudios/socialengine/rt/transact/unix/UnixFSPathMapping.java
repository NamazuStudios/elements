package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;

import static com.namazustudios.socialengine.rt.Path.fromContextAndComponents;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.DIRECTORY_SUFFIX;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.DIRECTORY;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_HARD_LINK;
import static java.lang.String.format;
import static java.nio.file.Files.isSymbolicLink;
import static java.nio.file.Files.readSymbolicLink;
import static java.util.stream.Collectors.joining;

public class UnixFSPathMapping {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSPathMapping.class);

    private final UnixFSUtils utils;

    private final Path fsPath;

    private final com.namazustudios.socialengine.rt.Path rtPath;

    public UnixFSPathMapping(final UnixFSUtils utils,
                             final Path fsPath, final com.namazustudios.socialengine.rt.Path rtPath) {
        this.utils = utils;
        this.fsPath = fsPath.toAbsolutePath().normalize();
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
    public static UnixFSPathMapping fromRTPath(final UnixFSUtils utils,
                                               final NodeId nodeId,
                                               final com.namazustudios.socialengine.rt.Path rtPath) {

        final com.namazustudios.socialengine.rt.Path fqPath = rtPath
            .getOptionalNodeId()
            .map(nid -> rtPath)
            .orElseGet(() -> rtPath.toPathWithContext(nodeId.asString()));

        final Path relative = utils.resolvePathStorageRoot(fqPath.getNodeId()).resolve(fqPath
            .stripWildcard()
            .getComponents()
            .stream()
            .map(component -> format("%s.%s", component, DIRECTORY_SUFFIX))
            .collect(joining(utils.getPathSeparator())));

        final Path fsPath = utils.getPathStorageRoot().resolve(relative).toAbsolutePath();
        return new UnixFSPathMapping(utils, fsPath, fqPath);

    }

    /**
     * Creates a {@link UnixFSPathMapping} with the supplied information which resolves a particular {@link Path} to a
     * {@link com.namazustudios.socialengine.rt.Path} mapping. This assumes that the supplied {@link Path} is fully
     * qualified in that the first component contains a stringified {@link NodeId}.
     *
     * This relativizes this against the the path storage root and then infers the {@link NodeId} from the first
     * component of the path.
     *
     * @param utils the {@link UnixFSUtils} used to determine the actual configured location
     * @param fsPath the {@link Path} representing the on-disk directory for the {@link com.namazustudios.socialengine.rt.Path}
     * @return the {@link UnixFSPathMapping} instance
     */
    public static UnixFSPathMapping fromFullyQualifiedFSPath(final UnixFSUtils utils, final Path fsPath) {
        return utils.doOperation(() -> {

            final Path relative = utils.getPathStorageRoot().relativize(fsPath);

            final NodeId nodeId;
            final Iterator<Path> pathIterator = relative.iterator();

            try {
                final String nodeIdString = pathIterator.next().toString();
                nodeId = NodeId.nodeIdFromString(nodeIdString);
            } catch (NoSuchElementException ex) {
                throw new IllegalArgumentException(format("Invalid FS path %s", fsPath), ex);
            }

            final List<String> components = new ArrayList<>();

            while (pathIterator.hasNext()) {
                final Path component = DIRECTORY.stripExtension(pathIterator.next());
                components.add(component.toString());
            }

            final com.namazustudios.socialengine.rt.Path rtPath = new com.namazustudios.socialengine.rt.Path(
                nodeId.asString(),
                components
            );

            return new UnixFSPathMapping(utils, fsPath, rtPath);

        }, FatalException::new);
    }

    /**
     * Creates a {@link UnixFSPathMapping} with the supplied information which resolves a particular {@link Path} to a
     * {@link com.namazustudios.socialengine.rt.Path} mapping. This relativizes this against the the path storage root
     * and then infers the {@link NodeId} from the first component of the path.
     *
     * @param utils the {@link UnixFSUtils} used to determine the actual configured location
     * @param fsPath the {@link Path} representing the on-disk directory for the {@link com.namazustudios.socialengine.rt.Path}
     * @return the {@link UnixFSPathMapping} instance
     */
    public static UnixFSPathMapping fromRelativeFSPath(final UnixFSUtils utils,
                                                       final NodeId nodeId,
                                                       final Path fsPath) {
        return utils.doOperation(() -> {

            final Path nodeDir = utils.resolvePathStorageRoot(nodeId);
            final Path relative = nodeDir.relativize(fsPath);

            final List<String> components = new ArrayList<>();

            relative.forEach(component -> {
                final Path stripped = DIRECTORY.stripExtension(component);
                components.add(stripped.toString());
            });

            final com.namazustudios.socialengine.rt.Path rtPath = new com.namazustudios.socialengine.rt.Path(
                nodeId.asString(),
                components
            );

            return new UnixFSPathMapping(utils, fsPath, rtPath);

        }, FatalException::new);
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
    public static UnixFSPathMapping fromFullyQualifiedSymlinkPath(final UnixFSUtils utils, final Path symlink) {
        return utils.doOperation(() -> {
            final Path target = readSymbolicLink(symlink);
            final Path fsPath = target.isAbsolute() ? target : symlink.getParent().resolve(target).normalize();
            return fromFullyQualifiedFSPath(utils, fsPath);
        }, FatalException::new);
    }

}
