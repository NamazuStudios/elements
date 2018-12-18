package com.namazustudios.socialengine.model.mission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a mission step.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@ApiModel
public class Step implements Serializable {

    @ApiModelProperty("The display name for the step")
    @NotNull
    private String displayName;

    @ApiModelProperty("The description of the step")
    @NotNull
    private String description;

    @NotNull
    @ApiModelProperty("The number of times the step must be completed to receive the reward(s)")
    @Min(value = 0, message = "Count may not be less than 1")
    private Integer count;

    @Null
    @ApiModelProperty("The reward(s) that will be granted upon completion")
    private List<Reward> rewards;

    @ApiModelProperty("The metadata for this step")
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count= count;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(final String name, final Object value) {

        if (getMetadata() == null) {
            setMetadata(new HashMap<>());
        }

        getMetadata().put(name, value);

    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Step)) return false;
        Step step = (Step) object;
        return Objects.equals(getDisplayName(), step.getDisplayName()) &&
                Objects.equals(getDescription(), step.getDescription()) &&
                Objects.equals(getCount(), step.getCount()) &&
                Objects.equals(getRewards(), step.getRewards()) &&
                Objects.equals(getMetadata(), step.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(), getDescription(), getCount(), getRewards(), getMetadata());
    }

    @Override
    public String toString() {
        return "Step{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", count=" + count +
                ", rewards=" + rewards +
                ", metadata=" + metadata +
                '}';
    }

}
