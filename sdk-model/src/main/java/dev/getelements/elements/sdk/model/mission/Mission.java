package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.Taggable;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Read;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a mission.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@Schema
public class Mission implements Serializable, Taggable {

    /** Creates a new instance. */
    public Mission() {}

    @Schema(description = "The unique ID of the mission")
    @Null(groups={Create.class, Insert.class})
    @NotNull(groups={Read.class})
    private String id;

    @Schema(description = "The name of the mission")
    @NotNull
    private String name;

    @Schema(description = "The display name for the mission")
    @NotNull
    private String displayName;

    @NotNull
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

    /**
     * Returns the unique ID of the mission.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the mission.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the mission.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the mission.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the display name of the mission.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the mission.
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
     * Returns the tags for this mission.
     *
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags for this mission.
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
     * Adds a metadata entry to this mission.
     *
     * @param name the metadata key
     * @param value the metadata value
     */
    public void addMetadata(final String name, final Object value) {

        if (getMetadata() == null) {
            setMetadata(new HashMap<>());
        }

        getMetadata().put(name, value);

    }

    /**
     * Returns the index for the given step, or -1 if not found.
     *
     * @param step the step to search for
     * @return index if found, or -1 if not found
     */
    public int getStepIndex(Step step) {
        for (int i=0; i<getSteps().size(); i++) {
            final Step missionStep = getSteps().get(i);
            if (missionStep.equals(step)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mission)) return false;

        Mission mission = (Mission) o;

        if (getId() != null ? !getId().equals(mission.getId()) : mission.getId() != null) return false;
        if (getName() != null ? !getName().equals(mission.getName()) : mission.getName() != null) return false;
        if (getDisplayName() != null ? !getDisplayName().equals(mission.getDisplayName()) : mission.getDisplayName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(mission.getDescription()) : mission.getDescription() != null) return false;
        if (getTags() != null ? !getTags().equals(mission.getTags()) : mission.getTags() != null) return false;
        if (getSteps() != null ? !getSteps().equals(mission.getSteps()) : mission.getSteps() != null) return false;
        if (getMetadata() != null ? !getMetadata().equals(mission.getMetadata()) : mission.getMetadata() != null) return false;
        return (getFinalRepeatStep() != null ? !getFinalRepeatStep().equals(mission.getFinalRepeatStep()) : mission.getFinalRepeatStep() != null);
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getTags() != null ? getTags().hashCode() : 0);
        result = 31 * result + (getSteps() != null ? getSteps().hashCode() : 0);
        result = 31 * result + (getFinalRepeatStep() != null ? getFinalRepeatStep().hashCode() : 0);
        result = 31 * result + (getMetadata() != null ? getMetadata().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Mission{" +
                ", id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags  + '\'' +
                ", steps='" + steps + '\'' +
                ", finalRepeatStep='" + finalRepeatStep + '\'' +
                ", metadata=" + metadata +
                '}';
    }

}