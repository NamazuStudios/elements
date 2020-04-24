package com.namazustudios.socialengine.rt.transact;

public interface RevisionDataStore extends AutoCloseable {

    PathIndex getPathIndex();

    ReversePathIndex getReversePathIndex();

    ResourceIdIndex getResourceIdIndex();

    void close();

    void apply(TransactionJournal.MutableEntry entry);

}
