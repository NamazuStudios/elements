package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Created by patricktwohig on 7/21/17.
 */
@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@SearchableDocument(
        fields = {
                @SearchableField(name = "scheme", path = "/scheme"),
                @SearchableField(name = "lastUpdatedTimestamp", path = "/lastUpdatedTimestamp"),
                @SearchableField(name = "playerId",  path = "/player/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class),
                @SearchableField(name = "opponentId",  path = "/opponent/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class)
        })
@Entity(value = "match", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field(value = "gameId")),
    @Index(fields = @Field(value = "lock.uuid")),
    @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = MongoMatch.MATCH_EXPIRATION_SECONDS))
})
public class MongoMatch {

    /**
     * The amount of seconds a {@link com.namazustudios.socialengine.model.match.Match}  This is set to something
     * considerably larger than what will ever be practically necessary, but allows clients to read a history of the
     * associated {@link MongoMatch} in order to sync state appropriately.  In reality a {@link MongoMatch} should
     * only need to live for a short period after match has been completed.
     */
    public static final int MATCH_EXPIRATION_SECONDS = 86400;

    @Id
    private ObjectId objectId;

    @Indexed
    @Reference
    private MongoProfile player;

    @Indexed
    @Property
    private String scheme;

    @Indexed
    @Reference
    private MongoProfile opponent;

    @Indexed
    @Property
    private Timestamp lastUpdatedTimestamp;

    @Property
    private String gameId;

    @Property
    private Timestamp expiry;

    @Indexed
    @Embedded
    private MongoMatchLock lock;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public MongoProfile getPlayer() {
        return player;
    }

    public void setPlayer(MongoProfile player) {
        this.player = player;
    }

    public MongoProfile getOpponent() {
        return opponent;
    }

    public void setOpponent(MongoProfile opponent) {
        this.opponent = opponent;
    }

    public Timestamp getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Timestamp lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public MongoMatchLock getLock() {
        return lock;
    }

    public void setLock(MongoMatchLock lock) {
        this.lock = lock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoMatch)) return false;
        MongoMatch that = (MongoMatch) o;
        return Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getPlayer(), that.getPlayer()) &&
                Objects.equals(getScheme(), that.getScheme()) &&
                Objects.equals(getOpponent(), that.getOpponent()) &&
                Objects.equals(getLastUpdatedTimestamp(), that.getLastUpdatedTimestamp()) &&
                Objects.equals(getGameId(), that.getGameId()) &&
                Objects.equals(getExpiry(), that.getExpiry()) &&
                Objects.equals(getLock(), that.getLock());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getObjectId(), getPlayer(), getScheme(), getOpponent(), getLastUpdatedTimestamp(), getGameId(), getExpiry(), getLock());
    }
}
