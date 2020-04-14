package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.*;

public class UnixFSRevisionDataStore implements RevisionDataStore {

    @Override
    public PathIndex getPathIndex() {
        return null;
    }

    @Override
    public ReversePathIndex getReversePathIndex() {
        return null;
    }

    @Override
    public ResourceIdIndex getResourceIndex() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void apply(final TransactionJournal.MutableEntry entry) {

    }

}
