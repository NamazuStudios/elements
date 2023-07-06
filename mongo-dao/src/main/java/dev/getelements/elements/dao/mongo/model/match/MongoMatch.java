package dev.getelements.elements.dao.mongo.model.match;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Created by patricktwohig on 7/21/17.
 */
@Entity(value = "match", useDiscriminator = false)
@Indexes({
    @Index(fields = @Field(value = "gameId")),
    @Index(fields = @Field(value = "lock.uuid")),
    @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = MongoMatch.MATCH_EXPIRATION_SECONDS))
})
public class MongoMatch {

    /**
     * The amount of seconds a {@link dev.getelements.elements.model.match.Match}  This is set to something
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
    @Property
    private String scope;

    @Indexed
    @Reference
    private MongoProfile opponent;

    @Indexed
    @Property
    private Date lastUpdatedTimestamp;

    @Property
    private String gameId;

    @Property
    private Date expiry;

    @Indexed
    private MongoMatchLock lock;

    private Map<String, Object> metadata;

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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

    public Date getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public MongoMatchLock getLock() {
        return lock;
    }

    public void setLock(MongoMatchLock lock) {
        this.lock = lock;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoMatch)) return false;
        MongoMatch that = (MongoMatch) object;
        return Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getPlayer(), that.getPlayer()) &&
                Objects.equals(getScheme(), that.getScheme()) &&
                Objects.equals(getScope(), that.getScope()) &&
                Objects.equals(getOpponent(), that.getOpponent()) &&
                Objects.equals(getLastUpdatedTimestamp(), that.getLastUpdatedTimestamp()) &&
                Objects.equals(getGameId(), that.getGameId()) &&
                Objects.equals(getExpiry(), that.getExpiry()) &&
                Objects.equals(getLock(), that.getLock()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getObjectId(), getPlayer(), getScheme(), getScope(), getOpponent(), getLastUpdatedTimestamp(), getGameId(), getExpiry(), getLock(), getMetadata());
    }

    @Override
    public String toString() {
        return "MongoMatch{" +
                "objectId=" + objectId +
                ", player=" + player +
                ", scheme='" + scheme + '\'' +
                ", scope='" + scope + '\'' +
                ", opponent=" + opponent +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", gameId='" + gameId + '\'' +
                ", expiry=" + expiry +
                ", lock=" + lock +
                ", metadata=" + metadata +
                '}';
    }

}
