package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * Mongo DTO for a mission.
 *
 * Created by davidjbrooks on 11/27/2018.
 */

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@SearchableDocument(fields = {
        @SearchableField(name = "name",         path = "/name"),
        @SearchableField(name = "displayName",  path = "/displayName"),
        @SearchableField(name = "description",  path = "/description")
})
@Entity(value = "mission", noClassnameStored = true)
public class MongoMission {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true))
    private String name;

    @Property
    private String displayName;

    @Property
    private String description;

    @Embedded
    private List<String> tags;

    @Embedded
    private List<MongoStep> steps;

    @Embedded
    private MongoStep finalRepeatStep;

    @Embedded
    private Map<String, Object> metadata;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId id) {
        this.objectId = id;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoMission)) return false;

        MongoMission mission = (MongoMission) o;

        if (getObjectId() != null ? !getObjectId().equals(mission.getObjectId()) : mission.getObjectId() != null) return false;
        if (getName() != null ? !getName().equals(mission.getName()) : mission.getName() != null) return false;
        if (getDisplayName() != null ? !getDisplayName().equals(mission.getDisplayName()) : mission.getDisplayName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(mission.getDescription()) : mission.getDescription() != null) return false;
        if (getTags() != null ? !getTags().equals(mission.getTags()) : mission.getTags() != null) return false;
        if (getSteps() != null ? !getSteps().equals(mission.getSteps()) : mission.getSteps() != null) return false;
        if (getFinalRepeatStep() != null ? !getFinalRepeatStep().equals(mission.getFinalRepeatStep()) : mission.getFinalRepeatStep() != null) return false;
        return (getMetadata() != null ? !getMetadata().equals(mission.getMetadata()) : mission.getMetadata() != null);
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getTags() != null ? getTags().hashCode() : 0);
        result = 31 * result + (getSteps() != null ? getSteps().hashCode() : 0);
        result = 31 * result + (getFinalRepeatStep() != null ? getFinalRepeatStep().hashCode() : 0);
        result = 31 * result + (getMetadata() != null ? getMetadata().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MongoMission{" +
                ", objectId='" + getObjectId() + '\'' +
                ", displayName='" + displayName + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tags='" + tags + '\'' +
                ", steps='" + steps + '\'' +
                ", finalRepeatStep='" + finalRepeatStep + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }

}
