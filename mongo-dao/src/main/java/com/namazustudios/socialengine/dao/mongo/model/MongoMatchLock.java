package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.UUID;

/**
 * A document type which locks a {@link MongoMatch} instance.  The {@link ObjectId} used with instance is acquired from
 * the {@link MongoMatch#getObjectId()}, however stored in a separate collection.  Additionally this contains a randomly
 * assigned {@link UUID} and a timestamp.
 *
 * The {@link UUID} guarantees that only the creator of the {@link MongoMatchLock} can destroy it
 *
 * Created by patricktwohig on 7/27/17.
 */
@Entity(value = "match_lock", noClassnameStored = true)
public class MongoMatchLock {

    public static final int PENDING_MATCH_TIMEOUT_SECONDS = 5;

    @Id
    private ObjectId playerMatchId;

//    @Property
//    @Indexed(options = @IndexOptions(expireAfterSeconds = PENDING_MATCH_TIMEOUT_SECONDS))
//    private Timestamp timestamp = new Timestamp(currentTimeMillis());

    @Property
    private String lockUuid;

    public MongoMatchLock() {}

    public MongoMatchLock(final ObjectId playerMatchId) {
        this.playerMatchId = playerMatchId;
        this.lockUuid = UUID.randomUUID().toString();
    }

    public ObjectId getPlayerMatchId() {
        return playerMatchId;
    }

    public void setPlayerMatchId(ObjectId playerMatchId) {
        this.playerMatchId = playerMatchId;
    }

//    public Timestamp getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(Timestamp timestamp) {
//        this.timestamp = timestamp;
//    }

    public String getLockUuid() {
        return lockUuid;
    }

    public void setLockUuid(String lockUuid) {
        this.lockUuid = lockUuid;
    }

}
