package com.namazustudios.socialengine.model.mission;

import com.namazustudios.socialengine.model.Taggable;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
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
@ApiModel
public class Mission implements Serializable, Taggable {

    @ApiModelProperty("The unique ID of the mission")
    @Null(groups={Create.class, Insert.class})
    @NotNull(groups={Update.class})
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

    @ApiModelProperty("The tags used to categorize this mission")
    private List<String> tags;

    @ApiModelProperty("The steps that constitute the mission (may be null if finalRepeatStep is specified)")
    private List<Step> steps;

    @ApiModelProperty("The final repeating step (may be null if step(s) are specified)")
    private Step finalRepeatStep;

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

    public void addMetadata(final String name, final Object value) {

        if (getMetadata() == null) {
            setMetadata(new HashMap<>());
        }

        getMetadata().put(name, value);

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