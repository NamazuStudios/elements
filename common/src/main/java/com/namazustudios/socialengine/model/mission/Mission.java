package com.namazustudios.socialengine.model.mission;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Represents a mission.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@ApiModel
public class Mission {

    @ApiModelProperty("The unique ID of the mission.")
    private String id;

    @ApiModelProperty("The name of the mission.")
    @NotNull
    private String name;

    @ApiModelProperty("The display name for the mission.")
    @NotNull
    private String displayName;

    @ApiModelProperty("The description of the mission.")
    @NotNull
    private String description;

    @ApiModelProperty("The steps that constitute the mission (may be null if finalRepeatStep is specified)")
    private Step[] steps;

    @ApiModelProperty("The final repeating step (optional)")
    private Step finalRepeatStep;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Step[] getSteps() {
        return steps;
    }

    public void setSteps(Step[] steps) {
        this.steps = steps;
    }

    public Step getFinalRepeatStep() {
        return finalRepeatStep;
    }

    public void setFinalRepeatStep(Step finalRepeatStep) {
        this.finalRepeatStep = finalRepeatStep;
    }

}