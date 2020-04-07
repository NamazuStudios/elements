package com.namazustudios.socialengine.rt.transact;

public interface RevisionDataStore extends AutoCloseable {

    PathIndex getPathIndex();

    ReversePathIndex getReversePathIndex();

    ResourceIndex getResourceIndex();

    void close();

}
