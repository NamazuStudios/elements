package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

import static java.lang.String.format;

public class UnixFSRevision<RevisionT> implements Revision<RevisionT> {

    private final long mask;

    private final long revision;

    private volatile String uid;

    public UnixFSRevision(final long mask, long revision) {
        this.mask = mask;
        this.revision = revision;
    }

    @Override
    public String getUniqueIdentifier() {
        return uid == null ? (uid = format("%0X", (mask & revision))) : uid;
    }

    @Override
    public int compareTo(final Revision<?> o) {
        return getUniqueIdentifier().compareTo(o.getUniqueIdentifier());
    }

}
