package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

import javax.inject.Provider;

import java.util.function.IntSupplier;

import static java.lang.String.format;

public class UnixFSRevision<RevisionT> implements Revision<RevisionT> {

    private volatile String uid;

    private final IntSupplier referenceSupplier;

    private final UnixFSDualCounter.Snapshot snapshot;

    UnixFSRevision(final IntSupplier referenceSupplier,
                   final UnixFSDualCounter.Snapshot snapshot) {
        this.snapshot = snapshot;
        this.referenceSupplier = referenceSupplier;
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
            final int reference = referenceSupplier.getAsInt();
            return snapshot.compareTo(reference, other.snapshot);
        }
    }

}
