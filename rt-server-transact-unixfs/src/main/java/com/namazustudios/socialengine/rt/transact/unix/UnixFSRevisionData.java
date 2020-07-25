package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.util.function.IntSupplier;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSDualCounter.Snapshot.fromIntegralValues;

public class UnixFSRevisionData extends Struct {

    public static final int SIZE = new UnixFSRevisionData().size();

    Signed32 max;

    Signed64 snapshot;

    void fromRevision(final UnixFSRevision<?> revision) {
        final UnixFSDualCounter.Snapshot snapshot = revision.getSnapshot();
        this.max.set(snapshot.getMax());
        this.snapshot.set(snapshot.getSnapshot());
    }

    UnixFSRevision toRevision(final IntSupplier referenceSupplier) {
        final UnixFSDualCounter.Snapshot snapshot = fromIntegralValues(this.max.get(), this.snapshot.get());
        return new UnixFSRevision(referenceSupplier, snapshot);
    }

}
