package com.namazustudios.socialengine.dao.mongo.model.mission;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mongo DTO for a mission step.
 *
 * This is NOT an entity, and is therefore not directly searchable
 *
 * As a purely embedded object, we could have leveraged the domain model - however, we may change our mind and create
 * a collection for this object and/or implement mongo-specific logic here
 *
 * Created by davidjbrooks on 11/27/2018.
 */
@Embedded
public class MongoStep {

    @Property
    private String displayName;

    @Property
    private String description;

    @Property
    private int count;

    @Property
    private List<MongoReward> rewards;

    private Map<String, Object> metadata;

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<MongoReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<MongoReward> rewards) {
        this.rewards = rewards;
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
        if (!(object instanceof MongoStep)) return false;
        MongoStep mongoStep = (MongoStep) object;
        return getCount() == mongoStep.getCount() &&
                Objects.equals(getDisplayName(), mongoStep.getDisplayName()) &&
                Objects.equals(getDescription(), mongoStep.getDescription()) &&
                Objects.equals(getRewards(), mongoStep.getRewards()) &&
                Objects.equals(getMetadata(), mongoStep.getMetadata());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getDisplayName(), getDescription(), getCount(), getRewards());
    }

    @Override
    public String toString() {
        return "MongoStep{" +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", rewards='" + rewards + '\'' +
                ", count='" + count + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
