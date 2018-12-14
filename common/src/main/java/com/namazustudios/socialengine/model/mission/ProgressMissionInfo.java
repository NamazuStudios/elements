package com.namazustudios.socialengine.model.mission;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public class ProgressMissionInfo {

    @ApiModelProperty("The name of the mission")
    @NotNull
    private String name;

    @ApiModelProperty("The display name for the mission")
    @NotNull
    private String displayName;

    @ApiModelProperty("The description of the mission")
    @NotNull
    private String description;

    @ApiModelProperty("The steps that constitute the mission (may be null if finalRepeatStep is specified)")
    private List<Step> steps;

    @ApiModelProperty("The final repeating step (may be null if step(s) are specified)")
    private Step finalRepeatStep;

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

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Step getFinalRepeatStep() {
        return finalRepeatStep;
    }

    public void setFinalRepeatStep(Step finalRepeatStep) {
        this.finalRepeatStep = finalRepeatStep;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ProgressMissionInfo)) return false;
        ProgressMissionInfo that = (ProgressMissionInfo) object;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDisplayName(), that.getDisplayName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getSteps(), that.getSteps()) &&
                Objects.equals(getFinalRepeatStep(), that.getFinalRepeatStep());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDisplayName(), getDescription(), getSteps(), getFinalRepeatStep());
    }

    @Override
    public String toString() {
        return "ProgressMissionInfo{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", steps=" + steps +
                ", finalRepeatStep=" + finalRepeatStep +
                '}';
    }

}
