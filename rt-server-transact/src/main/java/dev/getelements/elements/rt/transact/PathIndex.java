package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;

import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * Manages the index of paths to revisions {@link ResourceId}s as well as the inverse relationship.
 */
public interface PathIndex {

    /**
     * Gets the {@link RevisionMap<Path, ResourceId>} mapping {@link Path} instances to {@link ResourceId instances.
     *
     * @param nodeId the {@link NodeId} which to use for the path mapping.
     *
     * @return the {@link RevisionMap<Path, ResourceId>}
     */
    RevisionMap<Path, ResourceId> getRevisionMap(NodeId nodeId);

    /**
     * Gets the {@link RevisionMap<ResourceId, Set<Path>} mapping which links {@link ResourceId} isntances to the
     * various paths that reference them.
     *
     * @param nodeId the {@link NodeId} from which to get the {@link Path} instances
     * @return the {@link Revision<Path, Set<ResourceId>}
     */
    RevisionMap<ResourceId, Set<Path>> getReverseRevisionMap(NodeId nodeId);

    /**
     * Lists all associations between {@link Path} and {@link ResourceId}s. This inclues listing of direct path links
     * as well as wildcard path links.
     *
     * @param nodeId the scope of the {@link NodeId}
     * @param revision the {@link Revision<?>} to fetch.
     * @param path the {@link Path}, may be wildcard if searching for multiple paths.
     *
     * @return the {@link Revision<?>} containing the stream of listings.
     */
    Revision<Stream<ResourceService.Listing>> list(NodeId nodeId, Revision<?> revision, Path path);

}
