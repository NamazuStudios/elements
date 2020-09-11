package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;

import java.nio.file.Path;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_SYMBOLIC_LINK;

public class UnixFSReversePathMapping {

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

    public static UnixFSReversePathMapping fromNodeId(final UnixFSUtils utils, final NodeId nodeId) {
        return new UnixFSReversePathMapping(utils, nodeId);
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

    public Revision<Path> findLatestSymlink(Revision<?> revision, ResourceId resourceId) {
        final Path reverseDirectory = resolveReverseDirectory(resourceId);
        return utils.findLatestForRevision(reverseDirectory, revision, REVISION_SYMBOLIC_LINK);
    }

    public Path resolveSymlink(final Revision<?> revision, final ResourceId resourceId) {
        final Path reverseDirectory = resolveReverseDirectory(resourceId);
        return utils.resolveSymlinkPath(reverseDirectory, revision);
    }

}
