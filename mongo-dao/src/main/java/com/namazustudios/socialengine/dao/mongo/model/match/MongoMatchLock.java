package com.namazustudios.socialengine.dao.mongo.model.match;

import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;

/**
 * A document type which locks a {@link MongoMatch} instance.  The {@link ObjectId} used with instance is acquired from
 * the {@link MongoMatch#getObjectId()}, however stored in a separate collection.  Additionally this contains a randomly
 * assigned {@link UUID} and a timestamp.
 *
 * The {@link UUID} guarantees that only the creator of the {@link MongoMatchLock} can destroy it
 *
 * Created by patricktwohig on 7/27/17.
 */
@Embedded
public class MongoMatchLock {

    public static final int PENDING_MATCH_TIMEOUT_SECONDS = 5;

    @Indexed
    @Property
    private Date timestamp = new Date(currentTimeMillis());

    @Indexed
    @Property
    private String uuid = randomUUID().toString();

    public MongoMatchLock() {}

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
