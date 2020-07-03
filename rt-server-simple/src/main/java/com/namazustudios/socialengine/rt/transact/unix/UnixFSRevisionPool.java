package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;
import javolution.io.Struct;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.isRegularFile;

/**
 * Serializes the actual update of the {@link Revision<?>} of the total database. This ensures that each sequentially
 * provided {@link Revision<?>} is committed and updated in order. Additionally, this further ensures that the
 * {@link Revision<?>}s are committed in the same order in which they are issued.
 *
 * This approach does have some performance implications, specifically it forces that each revision be applied in order
 * therefore, threads attempting to commit may have to wait for others to complete. However, the implementation is
 * designed to be a lightweight approach, minimizing the chances that there is contention.
 */
public class UnixFSRevisionPool implements Revision.Factory, AutoCloseable {

    private final UnixFSDualCounter revisionCounter = new UnixFSDualCounter();

    /**
     * Creates a new {@link Revision<?>} and returns it. Once returned, the {@link Revision<?>} must be either committed
     * or canceled before future {@link Revision<?>}s will be applied
     * @return
     */
    public Revision<?> createNextRevision() {
        return new UnixFSRevision<>(revisionCounter::getTrailing, revisionCounter.incrementLeadingAndGetSnapshot());
    }

    @Override
    public UnixFSRevision<?> create(final String at) {
        final UnixFSDualCounter.Snapshot snapshot =  UnixFSDualCounter.Snapshot.fromString(at);
        return new UnixFSRevision<>(revisionCounter::getTrailing, snapshot);
    }

    @Override
    public void close() {}

}
