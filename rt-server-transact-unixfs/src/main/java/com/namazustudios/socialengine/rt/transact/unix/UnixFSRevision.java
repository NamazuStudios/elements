package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

import java.util.function.IntSupplier;

import static java.lang.String.format;

public class UnixFSRevision<RevisionT> implements Revision<RevisionT> {

    private volatile String uid;

    private final long value;

    UnixFSRevision(final long value) {
        this.value = value;
    }

    @Override
    public <RevisionT1 extends Revision> RevisionT1 getOriginal(final Class<RevisionT1> cls) {
        return cls.cast(this);
    }

    @Override
    public String getUniqueIdentifier() {
        return uid == null ? (uid = format("%016X", value)) : uid;
    }

    @Override
    public int compareTo(final Revision<?> o) {
        if (o.getOriginal() == Revision.ZERO) {
            return 1;
        } else if (o.getOriginal() == Revision.INFINITY) {
            return -1;
        } else {
            final UnixFSRevision<?> other = o.getOriginal(UnixFSRevision.class);
            return Long.compareUnsigned(value, other.value);
        }
    }

    /**
     * Returns the revision value as a long. For the purposes of this, we consider this a signed long value.
     *
     * @return the value as a long
     */
    public long asLong() {
        return value;
    }

    @Override
    public String toString() {
        return getUniqueIdentifier();
    }

}
