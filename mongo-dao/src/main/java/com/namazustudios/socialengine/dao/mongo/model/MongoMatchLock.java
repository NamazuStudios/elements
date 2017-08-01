package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;

import static java.lang.System.currentTimeMillis;

/**
 * A document type which represents a pending match.  These objects effectively lock two matches
 * for a brief period of time while the server completes the match.
 *
 * Created by patricktwohig on 7/27/17.
 */
@Entity(value = "match_lock", noClassnameStored = true)
public class MongoMatchLock {

    public static final int PENDING_MATCH_TIMEOUT_SECONDS = 5;

    @Id
    private ObjectId playerMatchId;

    @Property
    @Indexed(options = @IndexOptions(expireAfterSeconds = PENDING_MATCH_TIMEOUT_SECONDS))
    private Timestamp timestamp = new Timestamp(currentTimeMillis());

    public MongoMatchLock() {}

    public MongoMatchLock(ObjectId playerMatchId) {
        this.playerMatchId = playerMatchId;
    }

    public ObjectId getPlayerMatchId() {
        return playerMatchId;
    }

    public void setPlayerMatchId(ObjectId playerMatchId) {
        this.playerMatchId = playerMatchId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
