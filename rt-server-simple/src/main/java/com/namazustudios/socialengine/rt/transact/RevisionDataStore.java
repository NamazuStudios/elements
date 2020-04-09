package com.namazustudios.socialengine.rt.transact;

public interface RevisionDataStore extends AutoCloseable {

    PathIndex getPathIndex();

    ReversePathIndex getReversePathIndex();

    ResourceIdIndex getResourceIndex();

    void close();

    void apply(TransactionJournal.MutableEntry entry);

}
