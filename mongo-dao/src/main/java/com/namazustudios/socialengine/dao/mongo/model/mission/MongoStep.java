package com.namazustudios.socialengine.dao.mongo.model.mission;

import org.mongodb.morphia.annotations.Embedded;

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

    private String displayName;

    private String description;

    private int count;

    private java.util.List<MongoReward> rewards;

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

    public java.util.List<MongoReward> getRewards() {
        return rewards;
    }

    public void setRewards(java.util.List<MongoReward> rewards) {
        this.rewards = rewards;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoStep)) return false;
        MongoStep mongoStep = (MongoStep) object;
        return getCount() == mongoStep.getCount() &&
                Objects.equals(getDisplayName(), mongoStep.getDisplayName()) &&
                Objects.equals(getDescription(), mongoStep.getDescription()) &&
                Objects.equals(getRewards(), mongoStep.getRewards());
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
                '}';
    }
}
