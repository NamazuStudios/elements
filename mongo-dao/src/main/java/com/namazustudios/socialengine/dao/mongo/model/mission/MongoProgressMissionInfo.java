package com.namazustudios.socialengine.dao.mongo.model.mission;

import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Embedded
public class MongoProgressMissionInfo {

    @Indexed
    @Property
    private ObjectId missionId;

    @Indexed
    @Property
    private String name;

    @Property
    private String displayName;

    @Property
    private String description;

    @Embedded
    private List<MongoStep> steps;

    @Embedded
    private MongoStep finalRepeatStep;

    @Indexed
    @Property
    private Set<String> tags;

    @Property
    private Map<String, Object> metadata;

    public ObjectId getMissionId() {
        return missionId;
    }

    public void setMissionId(ObjectId missionId) {
        this.missionId = missionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MongoStep> getSteps() {
        return steps;
    }

    public void setSteps(List<MongoStep> steps) {
        this.steps = steps;
    }

    public MongoStep getFinalRepeatStep() {
        return finalRepeatStep;
    }

    public void setFinalRepeatStep(MongoStep finalRepeatStep) {
        this.finalRepeatStep = finalRepeatStep;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
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
        if (!(object instanceof MongoProgressMissionInfo)) return false;
        MongoProgressMissionInfo that = (MongoProgressMissionInfo) object;
        return Objects.equals(getMissionId(), that.getMissionId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDisplayName(), that.getDisplayName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getSteps(), that.getSteps()) &&
                Objects.equals(getFinalRepeatStep(), that.getFinalRepeatStep()) &&
                Objects.equals(getTags(), that.getTags()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMissionId(), getName(), getDisplayName(), getDescription(), getSteps(), getFinalRepeatStep(), getTags(), getMetadata());
    }

    @Override
    public String toString() {
        return "MongoProgressMissionInfo{" +
                "missionId=" + missionId +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", steps=" + steps +
                ", finalRepeatStep=" + finalRepeatStep +
                ", tags=" + tags +
                ", metadata=" + metadata +
                '}';
    }

}
