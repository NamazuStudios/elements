package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Objects;

@Entity(value = "progress_pending_award", noClassnameStored = true)
public class MongoPendingReward {

    private static final int EXPIRY_TIME_SECONDS = 60;

    @Id
    private ObjectId objectId;

    @Indexed
    private MongoUser user;

    @Reference
    private MongoProgress progress;

    @Embedded
    private MongoStep step;

    @Embedded
    private MongoReward reward;

    @Indexed(options = @IndexOptions(expireAfterSeconds = EXPIRY_TIME_SECONDS))
    private Timestamp expires;

    @Indexed
    @Property
    private State state;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoProgress getProgress() {
        return progress;
    }

    public void setProgress(MongoProgress progress) {
        this.progress = progress;
    }

    public MongoStep getStep() {
        return step;
    }

    public void setStep(MongoStep step) {
        this.step = step;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoPendingReward)) return false;
        MongoPendingReward that = (MongoPendingReward) object;
        return Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getProgress(), that.getProgress()) &&
                Objects.equals(getReward(), that.getReward()) &&
                Objects.equals(getExpires(), that.getExpires()) &&
                getState() == that.getState();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getProgress(), getReward(), getExpires(), getState());
    }

    @Override
    public String toString() {
        return "MongoPendingReward{" +
                "objectId=" + objectId +
                ", progress=" + progress +
                ", reward=" + reward +
                ", expires=" + expires +
                ", state=" + state +
                '}';
    }

    public enum State {

        /**
         * Indicates that the reward has been created, but not visible and pending.
         */
        CREATED,

        /**
         * Indicates that the reward is in a state of pending.
         */
        PENDING,

        /**
         * Indicates that the reward is in the rewarded state.
         */
        REWARDED

    }

}
