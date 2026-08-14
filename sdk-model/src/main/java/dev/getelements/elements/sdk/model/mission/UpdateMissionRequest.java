package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.Taggable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Represents a request to update an existing mission's properties. */
@Schema
public class UpdateMissionRequest implements Taggable {

    /** Creates a new instance. */
    public UpdateMissionRequest() {}

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

    @Schema(description = "Whether this mission's Progress is authoritative (server-only mutation via " +
            "Element code) or may be advanced directly by a client via the REST API. Omit to leave the " +
            "mission's current value unchanged.")
    private Boolean authoritative;

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
     * Returns whether this mission's progress is authoritative (server-only mutation).
     *
     * @return whether progress is authoritative, or null to leave unchanged
     */
    public Boolean getAuthoritative() {
        return authoritative;
    }

    /**
     * Sets whether this mission's progress is authoritative (server-only mutation).
     *
     * @param authoritative whether progress is authoritative
     */
    public void setAuthoritative(Boolean authoritative) {
        this.authoritative = authoritative;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UpdateMissionRequest that)) return false;
        return Objects.equals(displayName, that.displayName) && Objects.equals(description, that.description) && Objects.equals(tags, that.tags) && Objects.equals(steps, that.steps) && Objects.equals(finalRepeatStep, that.finalRepeatStep) && Objects.equals(metadata, that.metadata) && Objects.equals(authoritative, that.authoritative);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, description, tags, steps, finalRepeatStep, metadata, authoritative);
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
                ", authoritative=" + authoritative +
                '}';
    }
}
