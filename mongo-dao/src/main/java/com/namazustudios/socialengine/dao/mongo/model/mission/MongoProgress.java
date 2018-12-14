package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.List;
import java.util.Objects;

/**
 * Mongo DTO for a mission progress.
 *
 * Created by davidjbrooks on 12/04/2018.
 */

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@Entity(value = "progress", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field("mission.name"))
})
public class MongoProgress {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property()
    private String version;

    @Indexed
    @Reference
    private MongoProfile profile;

    @Embedded
    private MongoProgressMissionInfo mission;

    @Embedded
    private MongoStep currentStep;

    @Property
    private int remaining;

    @Property
    private int stepSequence;

    @Property
    private List<MongoReward> unclaimedRewards;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId id) {
        this.objectId = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public MongoStep getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(MongoStep currentStep) {
        this.currentStep = currentStep;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public MongoProgressMissionInfo getMission() {
        return mission;
    }

    public void setMission(MongoProgressMissionInfo mission) {
        this.mission = mission;
    }

    public int getStepSequence() {
        return stepSequence;
    }

    public void setStepSequence(int stepSequence) {
        this.stepSequence = stepSequence;
    }

    public List<MongoReward> getUnclaimedRewards() {
        return unclaimedRewards;
    }

    public void setUnclaimedRewards(List<MongoReward> unclaimedRewards) {
        this.unclaimedRewards = unclaimedRewards;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoProgress)) return false;
        MongoProgress that = (MongoProgress) object;
        return getRemaining() == that.getRemaining() &&
                getStepSequence() == that.getStepSequence() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getProfile(), that.getProfile()) &&
                Objects.equals(getMission(), that.getMission()) &&
                Objects.equals(getCurrentStep(), that.getCurrentStep()) &&
                Objects.equals(getUnclaimedRewards(), that.getUnclaimedRewards());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getObjectId(), getVersion(), getProfile(), getMission(), getCurrentStep(), getRemaining(), getStepSequence(), getUnclaimedRewards());
    }

    @Override
    public String toString() {
        return "MongoProgress{" +
                "objectId=" + objectId +
                ", version='" + version + '\'' +
                ", profile=" + profile +
                ", mission=" + mission +
                ", currentStep=" + currentStep +
                ", remaining=" + remaining +
                ", stepSequence=" + stepSequence +
                ", unclaimedRewards=" + unclaimedRewards +
                '}';
    }

}
