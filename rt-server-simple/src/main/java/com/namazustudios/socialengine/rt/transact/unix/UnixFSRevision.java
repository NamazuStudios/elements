package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

import javax.inject.Provider;

import static java.lang.String.format;

public class UnixFSRevision<RevisionT> implements Revision<RevisionT> {

    private volatile String uid;

    private final UnixFSDualCounter.Snapshot snapshot;

    private final Provider<UnixFSDualCounter.Snapshot> referenceProvider;

    public UnixFSRevision(final UnixFSDualCounter.Snapshot snapshot,
                          final Provider<UnixFSDualCounter.Snapshot> referenceProvider) {
        this.snapshot = snapshot;
        this.referenceProvider = referenceProvider;
    }

    @Override
    public <RevisionT1 extends Revision> RevisionT1 getOriginal(final Class<RevisionT1> cls) {
        return cls.cast(this);
    }

    @Override
    public String getUniqueIdentifier() {
        return uid == null ? (uid = format("%0X", snapshot.getSnapshot())) : uid;
    }

    @Override
    public int compareTo(final Revision<?> o) {
        if (o == Revision.ZERO) {
            return 1;
        } else if (o == Revision.INFINITY) {
            return -1;
        } else {
            final UnixFSRevision<?> other = o.getOriginal(UnixFSRevision.class);
            final UnixFSDualCounter.Snapshot reference = referenceProvider.get();
            return snapshot.compareTo(reference, other.snapshot);
        }
    }

}
