package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.Taggable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Schema
public class UpdateMissionRequest implements Taggable {

    @Schema(description = "The display name for the mission")
    private String displayName;

    @Schema(description = "The description of the mission")
    private String description;

    @Schema(description = "The tags used to categorize this mission")
    private List<String> tags;

    @Schema(description = "The steps that constitute the mission (may be null if finalRepeatStep is specified)")
    private List<Step> steps;

    @Schema(description = "The final repeating step (may be null if step(s) are specified)")
    private Step finalRepeatStep;

    @Schema(description = "The metadata for this mission")
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UpdateMissionRequest that)) return false;
        return Objects.equals(displayName, that.displayName) && Objects.equals(description, that.description) && Objects.equals(tags, that.tags) && Objects.equals(steps, that.steps) && Objects.equals(finalRepeatStep, that.finalRepeatStep) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, description, tags, steps, finalRepeatStep, metadata);
    }

    @Override
    public String toString() {
        return "UpdateMissionRequest{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", steps=" + steps +
                ", finalRepeatStep=" + finalRepeatStep +
                ", metadata=" + metadata +
                '}';
    }
}
