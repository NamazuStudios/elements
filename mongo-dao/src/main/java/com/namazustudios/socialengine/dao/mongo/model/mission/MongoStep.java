package com.namazustudios.socialengine.dao.mongo.model.mission;

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
public class MongoStep {
    private String displayName;

    private String description;

    private Integer count;

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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count= count;
    }

    public java.util.List<MongoReward> getRewards() {
        return rewards;
    }

    public void setRewards(java.util.List<MongoReward> rewards) {
        this.rewards = rewards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoStep)) return false;

        MongoStep mongoStep = (MongoStep) o;

        if (getDisplayName() != null ? !getDisplayName().equals(mongoStep.getDisplayName()) : mongoStep.getDisplayName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(mongoStep.getDescription()) : mongoStep.getDescription() != null) return false;
        if (getRewards() != null ? !getRewards().equals(mongoStep.getRewards()) : mongoStep.getRewards() != null) return false;
        return (getCount() != null ? !getCount().equals(mongoStep.getCount()) : mongoStep.getCount() != null);
    }

    @Override
    public int hashCode() {
        int result = (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getRewards() != null ? getRewards().hashCode() : 0);
        result = 31 * result + (getCount() != null ? getCount().hashCode() : 0);
        return result;
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
