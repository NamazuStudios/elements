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

    @Property
    private int sequence;

    @Property
    private int remaining;

    @Reference
    private List<MongoPendingReward> pendingRewards;

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
        final int sequence = getSequence();
        return getStepForSequence(sequence);
    }

    public MongoStep getStepForSequence(final int sequence) {

        final MongoProgressMissionInfo mission = getMission();
        final List<MongoStep> mongoSteps = mission.getSteps();

        return (mongoSteps == null || sequence >= mongoSteps.size()) ?
                mission.getFinalRepeatStep() :
                mongoSteps.get(sequence);

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

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public List<MongoPendingReward> getPendingRewards() {
        return pendingRewards;
    }

    public void setPendingRewards(List<MongoPendingReward> pendingRewards) {
        this.pendingRewards = pendingRewards;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoProgress)) return false;
        MongoProgress that = (MongoProgress) object;
        return getRemaining() == that.getRemaining() &&
                getSequence() == that.getSequence() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getProfile(), that.getProfile()) &&
                Objects.equals(getMission(), that.getMission()) &&
                Objects.equals(getCurrentStep(), that.getCurrentStep()) &&
                Objects.equals(getPendingRewards(), that.getPendingRewards());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getObjectId(), getProfile(), getMission(), getCurrentStep(), getRemaining(), getSequence(), getPendingRewards());
    }

    @Override
    public String toString() {
        return "MongoProgress{" +
                "objectId=" + objectId +
                ", version='" + version + '\'' +
                ", profile=" + profile +
                ", mission=" + mission +
                ", currentStep=" + getCurrentStep() +
                ", remaining=" + remaining +
                ", sequence=" + sequence +
                ", pendingRewards=" + pendingRewards +
                '}';
    }

}
