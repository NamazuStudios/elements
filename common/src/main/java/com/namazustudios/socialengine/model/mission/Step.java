package com.namazustudios.socialengine.model.mission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Represents a mission step.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@ApiModel
public class Step {

    @ApiModelProperty("The display name for the step.")
    @NotNull
    private String displayName;

    @ApiModelProperty("The description of the step.")
    @NotNull
    private String description;

    @NotNull
    @ApiModelProperty("The number of times the step must be completed to receive the reward(s)")
    @Min(value = 0, message = "Count may not be less than 1")
    private Integer count;

    @NotNull
    @ApiModelProperty("The Rewards that will be granted upon completion")
    private Reward[] rewards;

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

    public Reward[] getRewards() {
        return rewards;
    }

    public void setRewards(Reward[] rewards) {
        this.rewards = rewards;
    }

}
