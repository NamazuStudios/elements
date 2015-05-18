package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import org.apache.lucene.store.Lock;
import org.bson.Document;

import java.io.IOException;

/**
 * An implementation of a {@link Lock} stored in a {@link MongoCollection}.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class MongoLock extends Lock {

    private final MongoCollection<Document> lockCollection;

    private final Document document;

    public MongoLock(final MongoCollection<Document> lockCollection, final String name) {
        this.lockCollection = lockCollection.withWriteConcern(WriteConcern.SAFE);
        document = new Document();
        document.put("_id", name);
    }

    @Override
    public boolean obtain() throws IOException {
        try {
            lockCollection.insertOne(document);
            return true;
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {
                return false;
            } else {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            lockCollection.deleteOne(document);
        } catch (MongoException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean isLocked() throws IOException {
        try {
            return lockCollection.find(document).first() != null;
        } catch (MongoCommandException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String toString() {
        return "MongoLock{" +
                "lockCollection=" + lockCollection +
                ", document=" + document +
                '}';
    }

}
