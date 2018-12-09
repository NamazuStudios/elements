package com.namazustudios.socialengine.model.mission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Represents a mission step.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@ApiModel
public class Step {

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

    @NotNull
    @ApiModelProperty("The reward(s) that will be granted upon completion")
    private List<Reward> rewards;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Step)) return false;

        Step step = (Step) o;

        if (getDisplayName() != null ? !getDisplayName().equals(step.getDisplayName()) : step.getDisplayName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(step.getDescription()) : step.getDescription() != null) return false;
        if (getRewards() != null ? !getRewards().equals(step.getRewards()) : step.getRewards() != null) return false;
        return (getCount() != null ? !getCount().equals(step.getCount()) : step.getCount() != null);
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
        return "Step{" +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", rewards='" + rewards + '\'' +
                ", count='" + count + '\'' +
                '}';
    }

}
