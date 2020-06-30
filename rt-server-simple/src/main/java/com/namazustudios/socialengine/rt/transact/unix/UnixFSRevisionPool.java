package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

import java.util.concurrent.atomic.AtomicLong;

public class UnixFSRevisionPool implements Revision.Factory {

    private final UnixFSDualCounter revisions = new UnixFSDualCounter();

    public Revision<?> getCurrent() {
        return new UnixFSRevision<>(revisions.getSnapshot(), revisions::getSnapshot);
    }

    @Override
    public <T> Revision<T> create(final String at) {
        // TODO Implement
        return null;
    }

    public Pending beginRevisionChange() {
        return null;
    }

    public interface Pending {
        Revision<?> getRevision();
    }
}
