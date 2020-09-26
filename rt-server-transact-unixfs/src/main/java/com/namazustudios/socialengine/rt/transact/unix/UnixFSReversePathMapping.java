package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_SYMBOLIC_LINK;
import static com.namazustudios.socialengine.rt.util.Hex.encode;

public class UnixFSReversePathMapping {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final NodeId nodeId;

    private final UnixFSUtils utils;

    private UnixFSReversePathMapping(final UnixFSUtils utils, final NodeId nodeId) {
        this.utils = utils;
        this.nodeId = nodeId;
    }

    public static UnixFSReversePathMapping fromRTPath(final UnixFSUtils utils,
                                                      final com.namazustudios.socialengine.rt.Path rtPath) {

        final com.namazustudios.socialengine.rt.Path fqPath = rtPath
                .getOptionalNodeId()
                .map(nid -> rtPath)
                .orElseThrow(FatalException::new);

        return new UnixFSReversePathMapping(utils, fqPath.getNodeId());

    }

    public static UnixFSReversePathMapping fromRTPath(final UnixFSUtils utils,
                                                      final NodeId nodeId,
                                                      final com.namazustudios.socialengine.rt.Path rtPath) {

        final com.namazustudios.socialengine.rt.Path fqPath = rtPath
                .getOptionalNodeId()
                .map(nid -> rtPath)
                .orElseGet(() -> rtPath.toPathWithContext(nodeId.asString()));

        return new UnixFSReversePathMapping(utils, fqPath.getNodeId());

    }

    /**
     * Builds a {@link UnixFSReversePathMapping} from a {@link NodeId}.
     *
     * @param utils the utils object
     * @param nodeId the {@link NodeId}
     * @return the {@link UnixFSReversePathMapping}
     */
    public static UnixFSReversePathMapping fromNodeId(final UnixFSUtils utils, final NodeId nodeId) {
        return new UnixFSReversePathMapping(utils, nodeId);
    }

    /**
     * Resolves the link name of the s {@link com.namazustudios.socialengine.rt.Path} against the FS Path..
     *
     * @param fsPath {@link Path}
     * @param rtPath the {@link com.namazustudios.socialengine.rt.Path}
     * @return the a {@link Path} with the name.
     */
    public Path resolvePath(final Path fsPath, final com.namazustudios.socialengine.rt.Path rtPath) {

        final MessageDigest digest;

        try {
            digest = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new FatalException(e);
        }

        final byte[] output = digest.digest(rtPath.toByteArray());
        final String encoded = encode(output);
        final Path path = Paths.get(encoded);

        return fsPath.resolve(path);

    }

    /**
     * Given the {@link NodeId} this will resolve the matching reverse-mapping directory for the node id associated
     * with the reverse mapping.
     */
    public Path resolveReverseDirectory() {
        return utils.getReversePathStorageRoot().resolve(nodeId.asString());
    }

    /**
     * Given the {@link ResourceId} this will resolve the directory which contains the reverse mappings.
     *
     * @param resourceId the {@link ResourceId} against which to resolve the reverse mapping.
     * @return
     */
    public Path resolveReverseDirectory(final ResourceId resourceId) {
        return resolveReverseDirectory().resolve(resourceId.asString());
    }

    /**
     * Finds the latest symlink to the path containing the reverse listing of paths.
     *
     * @param revision the revision
     * @param resourceId the resource id
     *
     * @return the {@link Revision<Path>}
     */
    public Revision<Path> findLatestSymlink(Revision<?> revision, ResourceId resourceId) {
        final Path reverseDirectory = resolveReverseDirectory(resourceId);
        return utils.findLatestForRevision(reverseDirectory, revision, REVISION_SYMBOLIC_LINK);
    }

    /**
     * Resolves the {@link ResourceId} and the revision of hte reverse mapping.
     *
     * @param revision the revision
     * @param resourceId the resource id
     *
     * @return the {@link Revision<Path>}
     */
    public Path resolveSymlink(final Revision<?> revision, final ResourceId resourceId) {
        final Path reverseDirectory = resolveReverseDirectory(resourceId);
        return utils.resolveSymlinkPath(reverseDirectory, revision);
    }

}
