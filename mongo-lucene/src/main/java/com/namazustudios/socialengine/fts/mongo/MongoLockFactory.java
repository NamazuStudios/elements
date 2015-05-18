package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.MongoCollection;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.bson.Document;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoLockFactory extends LockFactory {

    private final MongoCollection<Document> lockCollection;

    public MongoLockFactory(final MongoCollection<Document> lockCollection) {
        this.lockCollection = lockCollection;
    }

    @Override
    public Lock makeLock(Directory dir, String lockName) {
        return new MongoLock(lockCollection, lockName);
    }

}
