package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

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

    @Property
    private MongoStep currentStep;

    @Property
    private Integer remaining;

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

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }

    public MongoMission getMission() {
        return mission;
    }

    public void setMission(MongoMission mission) {
        this.mission = mission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoMission)) return false;

        MongoProgress mongoProgress = (MongoProgress) o;

        if (getObjectId() != null ? !getObjectId().equals(mongoProgress.getObjectId()) : mongoProgress.getObjectId() != null) return false;
        if (getProfile() != null ? !getProfile().equals(mongoProgress.getProfile()) : mongoProgress.getProfile() != null) return false;
        if (getCurrentStep() != null ? !getCurrentStep().equals(mongoProgress.getCurrentStep()) : mongoProgress.getCurrentStep() != null) return false;
        if (getRemaining() != null ? !getRemaining().equals(mongoProgress.getRemaining()) : mongoProgress.getRemaining() != null) return false;
        return (getMission() != null ? !getMission().equals(mongoProgress.getMission()) : mongoProgress.getMission() != null);
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        result = 31 * result + (getCurrentStep() != null ? getCurrentStep().hashCode() : 0);
        result = 31 * result + (getRemaining() != null ? getRemaining().hashCode() : 0);
        result = 31 * result + (getMission() != null ? getMission().hashCode() : 0);
        return result;
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
