package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.bson.Document;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.namazustudios.socialengine.fts.mongo.MongoLock.EXPIRES_FIELD;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoLockFactory extends LockFactory {

    public static final String EXPIRY_INDEX = "lock_expire_idx";

    public static final long DEFAULT_LOCK_TIMEOUT_MILLISECONDS = 5000;

    private final long lockTimeoutMilliseconds;

    private final long heartbeatIntervalMilliseconds;

    private final MongoCollection<Document> lockCollection;

    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(MongoLockFactory.class.getSimpleName() + " heartbeat thread.");
        return thread;
    });

    public MongoLockFactory(final MongoCollection<Document> lockCollection) {
        this(lockCollection, DEFAULT_LOCK_TIMEOUT_MILLISECONDS);
    }

    public MongoLockFactory(final MongoCollection<Document> lockCollection, final long lockTimeoutMilliseconds) {

        this.lockCollection = lockCollection;
        this.lockTimeoutMilliseconds = lockTimeoutMilliseconds;
        this.heartbeatIntervalMilliseconds = lockTimeoutMilliseconds / 2;

        final Document index = new Document();
        index.put(EXPIRES_FIELD, 1);
        lockCollection.createIndex(index, new IndexOptions().name(EXPIRY_INDEX).expireAfter(0l, SECONDS));

    }

    @Override
    public Lock obtainLock(final Directory dir, final String lockName) throws IOException {

        final MongoLock lock;
        final Timestamp expiration = new Timestamp(currentTimeMillis() +  lockTimeoutMilliseconds);
        lock = new MongoLock(lockCollection, lockName, expiration);

        return lock;
    }

}
