package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Allows for fetching and opening of the {@link Resource} data.
 */
public interface ResourceIndex {

    /**
     * Checks if a {@link Resource} exists for hte supplied {@link ResourceId} and {@link Revision<?>}.  If the
     * {@link ResourceId} points to an entry, the supplied {@link Revision<Boolean>} will both be present and return
     * true.
     *
     * @param revision the revision reference
     * @param resourceId the {@link ResourceId} to test
     * @return true if the {@link ResourceId} exist at the supplied {@link Revision}
     */
    Revision<Boolean> existsAt(Revision<?> revision, ResourceId resourceId);

    /**
     * Attempts to open the contents of a {@link Resource} with the supplied {@link Path} at the supplied
     * {@link Revision<?>}. The returned {@link ReadableByteChannel} must be closed by the caller if it is present.
     *
     * @param revision the {@link Revision<?>} to reference
     * @param resourceId the {@link Path} to the {@link ResourceId}
     * @return a {@link Revision<ReadableByteChannel>} which can be used to read the {@link Resource} contents
     */
    Revision<ReadableByteChannel> loadResourceContentsAt(Revision<?> revision, ResourceId resourceId) throws IOException;

    /**
     * Given the file path, this updates the resource with the files's contents. The file should point to a temporary
     * file which will get moved to the correct revision and {@link ResourceId}
     *
     * @param revision
     * @param fsPath
     * @param resourceId
     * @param revision the {@link Revision<?>} to reference
     * @param resourceId the {@link Path} to the {@link ResourceId}
     */
    void updateResource(Revision<?> revision, java.nio.file.Path fsPath, ResourceId resourceId);

}
