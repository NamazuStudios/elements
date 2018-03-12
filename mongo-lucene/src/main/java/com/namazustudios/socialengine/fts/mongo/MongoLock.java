package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mongodb.client.model.Filters.*;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;

/**
 * An implementation of a {@link Lock} stored in a {@link MongoCollection}.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class MongoLock extends Lock {

    private static final Logger logger = LoggerFactory.getLogger(MongoLock.class);

    public static final String UUID_FIELD = "uuid";

    public static final String EXPIRES_FIELD = "expires";

    private final String name;

    private final MongoCollection<Document> lockCollection;

    private final AtomicBoolean open = new AtomicBoolean(true);

    private final String uuid = randomUUID().toString();

    public MongoLock(final MongoCollection<Document> lockCollection,
                     final String name,
                     final Timestamp expires) throws IOException {

        this.name = name;
        this.lockCollection = lockCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED);

        final Timestamp now = new Timestamp(currentTimeMillis());

        final Document document = new Document();
        document.put("_id", name);
        document.put(UUID_FIELD, uuid);
        document.put(EXPIRES_FIELD, expires);

        try {
            final Bson query = and(eq("_id", name), lt(EXPIRES_FIELD, now));
            lockCollection.findOneAndReplace(query, document, new FindOneAndReplaceOptions().upsert(true));
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {
                throw new LockObtainFailedException("Failed to obtain lock: " + name);
            } else {
                throw new IOException(ex);
            }
        }

    }

    @Override
    public void ensureValid() throws IOException {
        try {

            final Timestamp now = new Timestamp(currentTimeMillis());
            final Bson query = and(eq("_id", name), eq(UUID_FIELD, uuid), gt(EXPIRES_FIELD, now));

            if (lockCollection.find(query).first() == null) {
                throw new LockExpiredException("Lock no longer valid.");
            }

        } catch (MongoCommandException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (open.compareAndSet(true, false)) {
                final Timestamp now = new Timestamp(currentTimeMillis());
                final Bson query = and(eq("_id", name), eq(UUID_FIELD, uuid));
                final DeleteResult deleteResult = lockCollection.deleteOne(query);
                if (deleteResult.getDeletedCount() == 0) throw new IOException("Failed to release lock");
            } else {
                logger.warn("Attemping to close already closed MongoLock", new IllegalStateException());
            }
        } catch (MongoException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Checks if the lock has been closed.  The lock is closed once the {@link #close()} method is called on this
     * object.
     *
     * @return true if the lock is still open, false if closed
     */
    public boolean isOpen() {
        return open.get();
    }

    /**
     * Refrehes this {@link Lock}.
     */
    public void refresh(final Timestamp expires) {

        if (!isOpen()) {
            throw new LockClosedException("Lock is closed.");
        }

        logger.info("Refreshing lock {} with new expiry {}", name, expires);

        final Timestamp now = new Timestamp(currentTimeMillis());

        final Document document = new Document();
        document.put("_id", name);
        document.put(UUID_FIELD, uuid);
        document.put(EXPIRES_FIELD, expires);

        try {
            final Bson query = and(eq("_id", name), eq(UUID_FIELD, uuid), gt(EXPIRES_FIELD, now));
            final Document result;
            result = lockCollection.findOneAndReplace(query, document, new FindOneAndReplaceOptions().upsert(false));
            if (result == null) throw new LockRefreshException();
        } catch (MongoException ex) {
            throw new LockRefreshException(ex);
        }

    }

}
