package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;

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
     * {@link Revision<?>}.
     *
     * As this returns an instance of {@link ReadableByteChannel} the returned {@link Revision<ReadableByteChannel>}
     * must not acquire any resources (such as open file descriptor) until the caller fetches it.  The caller must also
     * take care to ensure that the {@link ReadableByteChannel#close()} method is called when it is finished.
     *
     * @param revision the {@link Revision<?>} to reference
     * @param path the {@link Path} to the {@link Resource}
     * @return a {@link Revision<ReadableByteChannel>} which can be used to read the {@link Resource} contents
     */
    Revision<ReadableByteChannel> loadResourceContentsAt(Revision<?> revision, Path path) throws IOException;

    /**
     * Attempts to open the contents of a {@link Resource} with the supplied {@link Path} at the supplied
     * {@link Revision<?>}.
     * s
     * As this returns an instance of {@link ReadableByteChannel} the returned {@link Revision<ReadableByteChannel>}
     * must not acquire any resources (such as open file descriptor) until the caller fetches it.  The caller must also
     * take care to ensure that the {@link ReadableByteChannel#close()} method is called when it is finished.
     *
     * @param revision the {@link Revision<?>} to reference
     * @param resourceId the {@link Path} to the {@link ResourceId}
     * @return a {@link Revision<ReadableByteChannel>} which can be used to read the {@link Resource} contents
     */
    Revision<ReadableByteChannel> loadResourceContentsAt(Revision<?> revision, ResourceId resourceId) throws IOException;

}
