package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.Revision;

import java.util.concurrent.atomic.AtomicLong;

public class UnixFSRevisionPool implements Revision.Factory {

    public Revision<?> create(final AtomicLong current) {
        // TODO Implement
        return null;
    }

    public Revision<?> nextRevision(final AtomicLong current) {
        // TODO Implement
        return null;
    }

    @Override
    public <T> Revision<T> create(final String at) {
        // TODO Implement
        return null;
    }

}
