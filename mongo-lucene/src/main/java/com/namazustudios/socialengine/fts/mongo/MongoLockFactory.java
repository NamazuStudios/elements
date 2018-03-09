package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockFactory;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static com.namazustudios.socialengine.fts.mongo.MongoLock.EXPIRES_FIELD;
import static com.namazustudios.socialengine.fts.mongo.MongoLock.UUID_FIELD;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoLockFactory extends LockFactory implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MongoLockFactory.class);

    public static final String UUID_INDEX_NAME = "uuid_idx";

    public static final String EXPIRY_INDEX_NAME = "lock_expire_idx";

    public static final long DEFAULT_LOCK_TIMEOUT_MILLISECONDS = 6000;

    public static final long DEFAULT_HEARTBEAT_INTERVAL_FACTOR = 3;

    public static final long DEFAULT_HEARTBEAT_INTERVAL_MILLISECONDS = DEFAULT_LOCK_TIMEOUT_MILLISECONDS / DEFAULT_HEARTBEAT_INTERVAL_FACTOR;

    public static final long LOCK_GARBAGE_COLLECTION_TIME_MILLISECONDS = 30000;

    private final long lockTimeoutMilliseconds;

    private final MongoCollection<Document> lockCollection;

    private final long heartbeatIntervalMilliseconds;

    private final AtomicBoolean open = new AtomicBoolean(true);

    private final Set<MongoLock> mongoLockSet = new HashSet<>();

    private final java.util.concurrent.locks.Lock shutdownLock = new ReentrantLock();

    private final java.util.concurrent.locks.Condition shutdownCondition = shutdownLock.newCondition();

    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(MongoLockFactory.class.getSimpleName() + " heartbeat thread.");
        return thread;
    });

    private void checkOpenAndThrowIfNecessary() {
        if (!open.get()) {
            throw new IllegalStateException("Lock factory is closed for business.");
        }
    }

    public MongoLockFactory(final MongoCollection<Document> lockCollection) {
        this(lockCollection, DEFAULT_LOCK_TIMEOUT_MILLISECONDS, DEFAULT_HEARTBEAT_INTERVAL_MILLISECONDS);
    }

    public MongoLockFactory(final MongoCollection<Document> lockCollection,
                            final long lockTimeoutMilliseconds) {
        this(lockCollection, lockTimeoutMilliseconds, lockTimeoutMilliseconds / DEFAULT_HEARTBEAT_INTERVAL_FACTOR);
    }

    public MongoLockFactory(final MongoCollection<Document> lockCollection,
                            final long lockTimeoutMilliseconds,
                            final long heartbeatIntervalMilliseconds) {

        this.lockCollection = lockCollection;
        this.lockTimeoutMilliseconds = lockTimeoutMilliseconds;
        this.heartbeatIntervalMilliseconds = heartbeatIntervalMilliseconds;

        final Document uuidIndex = new Document();
        uuidIndex.put(UUID_FIELD, 1);
        lockCollection.createIndex(uuidIndex, new IndexOptions()
            .name(UUID_INDEX_NAME));

        final Document expiryIndex = new Document();
        expiryIndex.put(EXPIRES_FIELD, 1);
        lockCollection.createIndex(expiryIndex, new IndexOptions()
            .name(EXPIRY_INDEX_NAME)
            .expireAfter(LOCK_GARBAGE_COLLECTION_TIME_MILLISECONDS, MILLISECONDS));

        getRuntime().addShutdownHook(new Thread(this::close));

    }

    @Override
    public MongoLock obtainLock(final Directory dir, final String lockName) throws IOException {

        if (!(dir instanceof GridFSDirectory)) {
            throw new IllegalArgumentException("dir must be " + GridFSDirectory.class.getSimpleName());
        }

        checkOpenAndThrowIfNecessary();

        final Timestamp expiration = new Timestamp(currentTimeMillis() +  getLockTimeoutMilliseconds());
        final AtomicReference<MongoLock> mongoLockAtomicReference = new AtomicReference<>();
        final AtomicReference<ScheduledFuture<?>> scheduledFutureAtomicReference = new AtomicReference<>();

        final String fqn = ((GridFSDirectory) dir).getIndexGridFSbucket().getBucketName() + "-" + lockName;

        mongoLockAtomicReference.set(new MongoLock(lockCollection, fqn, expiration) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    deregister(mongoLockAtomicReference.get());
                    scheduledFutureAtomicReference.get().cancel(false);
                }
            }
        });

        scheduledFutureAtomicReference.set(heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                final Timestamp nextExpiration = new Timestamp(currentTimeMillis() + lockTimeoutMilliseconds);
                mongoLockAtomicReference.get().refresh(nextExpiration);
            } catch (Exception ex) {
                logger.error("Exception caught refreshing lock.", ex);
            }
        }, getHeartbeatIntervalMilliseconds(), getHeartbeatIntervalMilliseconds(), MILLISECONDS));

        register(mongoLockAtomicReference.get());
        return mongoLockAtomicReference.get();

    }

    private void register(final MongoLock mongoLock) {
        try {
            shutdownLock.lock();
            mongoLockSet.add(mongoLock);
            shutdownCondition.signalAll();
        } finally {
            shutdownLock.unlock();
        }
    }

    private void deregister(final MongoLock mongoLock) {
        try {
            shutdownLock.lock();
            mongoLockSet.remove(mongoLock);
            shutdownCondition.signalAll();
        } finally {
            shutdownLock.unlock();
        }
    }

    /**
     * Gracefully closes this {@link MongoLockFactory} and waits for all locks to release.  This is useful for ensuring
     * that the index is gracefully unlocked if the factory exits.
     */
    @Override
    public void close() {
        if (open.compareAndSet(true, false)) {
            heartbeatScheduler.shutdown();
            waitForLocksToRelease();
        }
    }

    /**
     * Closes the {@link MongoLockFactory} now by simply terminating the heartbeat thread.
     */
    public void closeNow() {
        if (open.compareAndSet(true, false)) {
            heartbeatScheduler.shutdown();
        } else {
            logger.warn("Already closed MongoLockFactory.", new IllegalStateException());
        }
    }

    /**
     * Invoked after a call to {@link #closeNow()}, this will wait for all {@link MongoLock} instances known to this
     * {@link MongoLockFactory} to close.  This will throw an instance of {@link IllegalStateException} if the
     * {@link MongoLockFactory} has not been closed yet.
     */
    public void waitForLocksToRelease() {

        if (open.get()) {
            throw new IllegalStateException("MongoLockFactory is still open.");
        }

        try {

            shutdownLock.lock();

            while (!mongoLockSet.isEmpty()) {
                shutdownCondition.await();
            }

        } catch (InterruptedException e) {
            throw new ShutdownException(e);
        } finally {
            shutdownLock.unlock();
        }
    }

    public MongoCollection<Document> getLockCollection() {
        return lockCollection;
    }

    public long getLockTimeoutMilliseconds() {
        return lockTimeoutMilliseconds;
    }

    public long getHeartbeatIntervalMilliseconds() {
        return heartbeatIntervalMilliseconds;
    }

}
