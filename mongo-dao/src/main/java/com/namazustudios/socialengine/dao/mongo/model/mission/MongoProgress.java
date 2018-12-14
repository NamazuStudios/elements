package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

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

    @Reference
    private MongoProfile profile;

    @Embedded
    private MongoStep currentStep;

    @Property
    private int remaining;

    @Property
    private MongoMission mission;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId id) {
        this.objectId = id;
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

    public MongoMission getMission() {
        return mission;
    }

    public void setMission(MongoMission mission) {
        this.mission = mission;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoProgress)) return false;
        MongoProgress that = (MongoProgress) object;
        return getRemaining() == that.getRemaining() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getProfile(), that.getProfile()) &&
                Objects.equals(getCurrentStep(), that.getCurrentStep()) &&
                Objects.equals(getMission(), that.getMission());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getProfile(), getCurrentStep(), getRemaining(), getMission());
    }

    @Override
    public String toString() {
        return "MongoProgress{" +
                ", objectId='" + getObjectId() + '\'' +
                ", profile='" + profile + '\'' +
                ", currentStep='" + currentStep + '\'' +
                ", remaining='" + remaining + '\'' +
                ", mission='" + mission + '\'' +
                '}';
    }

}
