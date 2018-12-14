package com.namazustudios.socialengine.dao.mongo.model.mission;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity(value = "progress_pending_award", noClassnameStored = true)
public class MongoPendingReward {

    private static final int EXPIRY_TIME_SECONDS = 60;

    @Id
    private ObjectId objectId;

    @Indexed
    @Reference
    private MongoProgress progress;

    @Embedded
    private MongoReward reward;

    @Indexed(options = @IndexOptions(expireAfterSeconds = EXPIRY_TIME_SECONDS))
    private Timestamp expires;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoProgress getProgress() {
        return progress;
    }

    public void setProgress(MongoProgress progress) {
        this.progress = progress;
    }

    public MongoReward getReward() {
        return reward;
    }

    public void setReward(MongoReward reward) {
        this.reward = reward;
    }

    public Timestamp getExpires() {
        return expires;
    }

    public void setExpires(Timestamp expires) {
        this.expires = expires;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoPendingReward)) return false;
        MongoPendingReward that = (MongoPendingReward) object;
        return Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(progress, that.progress) &&
                Objects.equals(getReward(), that.getReward()) &&
                Objects.equals(getExpires(), that.getExpires());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), progress, getReward(), getExpires());
    }

    @Override
    public String toString() {
        return "MongoPendingReward{" +
                "objectId=" + objectId +
                ", progress=" + progress +
                ", reward=" + reward +
                ", expires=" + expires +
                '}';
    }

}
