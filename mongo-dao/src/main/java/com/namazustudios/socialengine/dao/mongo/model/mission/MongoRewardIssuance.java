package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.mission.RewardIssuance.Type;
import com.namazustudios.socialengine.model.mission.RewardIssuance.State;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Entity(value = "progress_pending_award", noClassnameStored = true)
public class MongoRewardIssuance {

    private static final int EXPIRY_TIME_SECONDS = 0;

    @Id
    private MongoRewardIssuanceId objectId;

    @Indexed
    @Reference
    private MongoUser user;

    @Indexed
    private State state;

    @Reference
    private MongoReward reward;

    @Indexed
    private String context;

    private Type type;

    private String source;

    private Map<String, Object> metadata;

    @Embedded
    private List<String> tags;

    @Indexed(options = @IndexOptions(expireAfterSeconds = 0))
    private Timestamp expirationTimestamp;

    @Indexed
    private String uuid;

    public MongoRewardIssuanceId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoRewardIssuanceId objectId) {
        this.objectId = objectId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoReward getReward() {
        return reward;
    }

    public void setReward(MongoReward reward) {
        this.reward = reward;
    }

    public Timestamp getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(Timestamp expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoRewardIssuance that = (MongoRewardIssuance) o;
        return Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                getState() == that.getState() &&
                Objects.equals(getReward(), that.getReward()) &&
                Objects.equals(getContext(), that.getContext()) &&
                getType() == that.getType() &&
                Objects.equals(getSource(), that.getSource()) &&
                Objects.equals(getMetadata(), that.getMetadata()) &&
                Objects.equals(getTags(), that.getTags()) &&
                Objects.equals(getExpirationTimestamp(), that.getExpirationTimestamp()) &&
                Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getUser(), getState(), getReward(), getContext(),
                getType(), getSource(), getMetadata(), getTags(), getExpirationTimestamp(),
                getUuid());
    }

    @Override
    public String toString() {
        return "MongoRewardIssuance{" +
                "objectId=" + objectId +
                ", user=" + user +
                ", state=" + state +
                ", reward=" + reward +
                ", context='" + context + '\'' +
                ", type=" + type +
                ", source='" + source + '\'' +
                ", metadata=" + metadata +
                ", tags=" + tags +
                ", expirationTimestamp=" + expirationTimestamp +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
