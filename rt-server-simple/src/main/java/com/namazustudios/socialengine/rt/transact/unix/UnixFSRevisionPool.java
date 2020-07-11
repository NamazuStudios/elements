package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

/**
 * Manages a circular buffer of available revisions, tracks the reference revision, and ensures that all revisions
 * properly sort at any given time. This further safeguards against creating invalid or out of range revisions.
 *
 */
public class UnixFSRevisionPool implements Revision.Factory, AutoCloseable {

    private final UnixFSDualCounter revisionCounter = new UnixFSDualCounter();

    /**
     * Creates a new {@link Revision<?>} and returns it. Once returned, the {@link Revision<?>} must be either committed
     * or canceled before future {@link Revision<?>}s will be applied
     * @return
     */
    public UnixFSRevision<?> createNextRevision() {
        return new UnixFSRevision<>(revisionCounter::getTrailing, revisionCounter.incrementLeadingAndGetSnapshot());
    }

    /**
     * Creates a {@link UnixFSRevision<?>} from the supplied {@link UnixFSRevisionData} (serialized form).
     *
     * @param unixFSRevisionData the {@link UnixFSRevisionData} representing the serialized form
     * @return the newly created {@link UnixFSRevision<?>}
     */
    public UnixFSRevision<?> create(final UnixFSRevisionData unixFSRevisionData) {
        return unixFSRevisionData.toRevision(revisionCounter::getTrailing);
    }

    @Override
    public UnixFSRevision<?> create(final String at) {
        final UnixFSDualCounter.Snapshot snapshot =  UnixFSDualCounter.Snapshot.fromString(at);
        return new UnixFSRevision<>(revisionCounter::getTrailing, snapshot);
    }

    @Override
    public void close() {}

}
