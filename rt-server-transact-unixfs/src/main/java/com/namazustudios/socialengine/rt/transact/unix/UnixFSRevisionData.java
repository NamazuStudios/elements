package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.util.function.IntSupplier;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSDualCounter.Snapshot.fromIntegralValues;

public class UnixFSRevisionData extends Struct {

    public static final int SIZE = new UnixFSRevisionData().size();

    final Signed64 value = new Signed64();

    void fromRevision(final UnixFSRevision<?> revision) {
        final long value = revision.asLong();
        this.value.set(value);
    }

    UnixFSRevision<?> toRevision() {
        final long value = this.value.get();
        return new UnixFSRevision(value);
    }

}
