package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.mission.PendingReward;
import com.namazustudios.socialengine.model.mission.PendingReward.State;
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
    @Reference
    private MongoUser user;

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
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getStep(), that.getStep()) &&
                Objects.equals(getReward(), that.getReward()) &&
                Objects.equals(getExpires(), that.getExpires()) &&
                getState() == that.getState();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getUser(), getStep(), getReward(), getExpires(), getState());
    }

    @Override
    public String toString() {
        return "MongoPendingReward{" +
                "objectId=" + objectId +
                ", user=" + user +
                ", step=" + step +
                ", reward=" + reward +
                ", expires=" + expires +
                ", state=" + state +
                '}';
    }

}
