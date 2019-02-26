package com.namazustudios.socialengine.model.mission;

import com.namazustudios.socialengine.model.Taggable;
import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ProgressMissionInfo implements Serializable, Taggable {

    @ApiModelProperty("The id of the mission")
    @NotNull
    private String id;

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

    @ApiModelProperty("The tags used to categorize this mission")
    private List<String> tags;

    @ApiModelProperty("The metadata for this mission")
    private Map<String, Object> metadata;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the index for the given step, or -1 if not found.
     *
     * @param step
     * @return index if found, or -1 if not found.
     */
    public int getStepSequence(Step step) {
        for (int i=0; i<getSteps().size(); i++) {
            final Step missionStep = getSteps().get(i);
            if (missionStep.equals(step)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ProgressMissionInfo)) return false;
        ProgressMissionInfo that = (ProgressMissionInfo) object;
        return Objects.equals(getId(), that.getId()) &&
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
        return Objects.hash(getId(), getName(), getDisplayName(), getDescription(), getSteps(), getFinalRepeatStep(), getTags(), getMetadata());
    }

    @Override
    public String toString() {
        return "ProgressMissionInfo{" +
                "id='" + id + '\'' +
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
