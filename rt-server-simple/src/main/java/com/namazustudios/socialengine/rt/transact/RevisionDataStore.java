package com.namazustudios.socialengine.rt.transact;

public interface RevisionDataStore extends AutoCloseable {

    PathIndex getPathIndex();

    ResourceIndex getResourceIndex();

    void close();

    void apply(TransactionJournal.MutableEntry entry);

}
