package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.Taggable;
import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Represents the mission information embedded within a progress record. */
public class ProgressMissionInfo implements Serializable, Taggable {

    /** Creates a new instance. */
    public ProgressMissionInfo() {}

    @Schema(description = "The id of the mission")
    @NotNull
    private String id;

    @Schema(description = "The name of the mission")
    @NotNull
    private String name;

    @Schema(description = "The display name for the mission")
    @NotNull
    private String displayName;

    @Schema(description = "The description of the mission")
    @NotNull
    private String description;

    @Schema(description = "The steps that constitute the mission (may be null if finalRepeatStep is specified)")
    private List<Step> steps;

    @Schema(description = "The final repeating step (may be null if step(s) are specified)")
    private Step finalRepeatStep;

    @Schema(description = "The tags used to categorize this mission")
    private List<String> tags;

    @Schema(description = "The metadata for this mission")
    private Map<String, Object> metadata;

    /**
     * Returns the mission ID.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the mission ID.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique name of the mission.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the mission.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the display name for the mission.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for the mission.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the description of the mission.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the mission.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the steps that constitute the mission.
     *
     * @return the steps
     */
    public List<Step> getSteps() {
        return steps;
    }

    /**
     * Sets the steps that constitute the mission.
     *
     * @param steps the steps
     */
    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    /**
     * Returns the final repeating step.
     *
     * @return the final repeat step
     */
    public Step getFinalRepeatStep() {
        return finalRepeatStep;
    }

    /**
     * Sets the final repeating step.
     *
     * @param finalRepeatStep the final repeat step
     */
    public void setFinalRepeatStep(Step finalRepeatStep) {
        this.finalRepeatStep = finalRepeatStep;
    }

    /**
     * Returns the tags used to categorize this mission.
     *
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags used to categorize this mission.
     *
     * @param tags the tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the metadata for this mission.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for this mission.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the index for the given step, or -1 if not found.
     *
     * @param step the step to find
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
